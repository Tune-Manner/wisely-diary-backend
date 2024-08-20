package tuneandmanner.wiselydiarybackend.common.vectorstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.jdbc.core.RowMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CustomPgVectorStore extends PgVectorStore implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(CustomPgVectorStore.class);

    private final String tableName;
    private final PgIndexType createIndexMethod;
    private final JdbcTemplate jdbcTemplate;
    private final PgDistanceType distanceType;
    private final int dimensions;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    public CustomPgVectorStore(JdbcTemplate jdbcTemplate,
                               EmbeddingModel embeddingModel,
                               int dimensions,
                               PgDistanceType distanceType,
                               boolean removeExistingVectorStoreTable,
                               PgIndexType createIndexMethod,
                               boolean initializeSchema,
                               String tableName,
                               ObjectMapper objectMapper) {
        super(jdbcTemplate, embeddingModel, dimensions, distanceType, removeExistingVectorStoreTable, createIndexMethod, false);
        this.tableName = tableName;
        this.createIndexMethod = createIndexMethod;
        this.jdbcTemplate = jdbcTemplate;
        this.distanceType = distanceType;
        this.dimensions = dimensions;
        this.embeddingModel = embeddingModel;
        this.objectMapper = objectMapper;
    }

    public EmbeddingModel getEmbeddingModel() {
        return this.embeddingModel;
    }

    private static final double SIMILARITY_THRESHOLD = 0.95; // 유사도 임계값, 필요에 따라 조정 가능

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Initializing CustomPgVectorStore for table: {}", tableName);
        // HNSW 인덱스 생성
        String createIndexSql = String.format(
                "CREATE INDEX IF NOT EXISTS hnsw_%s_embedding ON %s USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64)",
                tableName, tableName
        );
        logger.info("Executing index creation SQL: {}", createIndexSql);
        jdbcTemplate.execute(createIndexSql);
        logger.info("Index created successfully for table: {}", tableName);
    }

    public boolean isDocumentSimilar(List<Double> newEmbedding) {
        String sql = String.format(
                "SELECT 1 - (embedding <-> ?::vector) as similarity " +
                        "FROM %s " +
                        "ORDER BY embedding <-> ?::vector " +
                        "LIMIT 1", tableName);

        try {
            List<Double> similarities = jdbcTemplate.query(
                    sql,
                    ps -> {
                        Array array = ps.getConnection().createArrayOf("float4", newEmbedding.toArray());
                        ps.setArray(1, array);
                        ps.setArray(2, array);
                    },
                    (rs, rowNum) -> rs.getDouble("similarity")
            );
            return !similarities.isEmpty() && similarities.get(0) > SIMILARITY_THRESHOLD;
        } catch (Exception e) {
            logger.error("Error checking document similarity", e);
            return false;
        }
    }

    public boolean isDocumentExists(String contentHash) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE content_hash = ?", tableName);
        logger.debug("Executing SQL query: {}", sql);
        logger.debug("Content hash: {}", contentHash);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, contentHash);
        boolean exists = count != null && count > 0;
        logger.debug("Document exists: {}", exists);
        return exists;
    }

    @Override
    public void add(List<Document> documents) {
        List<Document> nonDuplicateDocuments = documents.stream()
                .filter(doc -> {
                    List<Double> embedding = embeddingModel.embed(doc.getContent());
                    return !isDocumentSimilar(embedding);
                })
                .collect(Collectors.toList());

        logger.debug("Number of non-duplicate documents: {}", nonDuplicateDocuments.size());

        if (nonDuplicateDocuments.isEmpty()) {
            logger.info("No new documents to add. All documents are similar to existing ones.");
            return;
        }

        String sql = String.format("INSERT INTO %s (id, content, metadata, embedding, content_hash) VALUES (?, ?, ?::json, ?, ?)", tableName);
        logger.info("Executing batch insert into table: {}", tableName);

        int[] updateCounts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Document document = nonDuplicateDocuments.get(i);
                List<Double> embedding = embeddingModel.embed(document.getContent());
                String contentHash = calculateContentHash(document.getContent());

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, document.getContent());
                try {
                    ps.setString(3, objectMapper.writeValueAsString(document.getMetadata()));
                } catch (JsonProcessingException e) {
                    throw new SQLException("Error converting metadata to JSON", e);
                }
                ps.setArray(4, ps.getConnection().createArrayOf("float4", embedding.toArray()));
                ps.setString(5, contentHash);
            }

            @Override
            public int getBatchSize() {
                return nonDuplicateDocuments.size();
            }
        });

        int insertedCount = Arrays.stream(updateCounts).sum();
        logger.info("Successfully inserted {} non-duplicate documents into table: {}", insertedCount, tableName);
    }

    private String calculateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating content hash", e);
        }
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        List<Double> queryEmbedding = embeddingModel.embed(request.getQuery());
        String sql = String.format(
                "SELECT id, content, metadata, embedding <-> CAST(? AS vector) as distance " +
                        "FROM %s " +
                        "ORDER BY embedding <-> CAST(? AS vector) " +
                        "LIMIT ?",
                tableName);

        return jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(sql);
                    Array array1 = con.createArrayOf("float4", queryEmbedding.toArray());
                    Array array2 = con.createArrayOf("float4", queryEmbedding.toArray());
                    ps.setArray(1, array1);
                    ps.setArray(2, array2);
                    ps.setInt(3, request.getTopK());
                    return ps;
                },
                (rs, rowNum) -> {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String content = rs.getString("content");
                    String metadataJson = rs.getString("metadata");
                    Map<String, Object> metadata = parseMetadata(metadataJson);
                    return new Document(id.toString(), content, metadata);
                }
        );
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing metadata JSON", e);
        }
    }

    // 필요한 경우 다른 메서드들도 오버라이드하여 tableName을 사용하도록 수정할 수 있습니다.
}