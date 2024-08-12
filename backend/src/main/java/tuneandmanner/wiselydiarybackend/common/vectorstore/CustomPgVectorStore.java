package tuneandmanner.wiselydiarybackend.common.vectorstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class CustomPgVectorStore extends PgVectorStore implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(CustomPgVectorStore.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // 스레드 풀 크기는 필요에 따라 조정

    private final String tableName;
    private final PgIndexType createIndexMethod;
    private final JdbcTemplate jdbcTemplate;
    private final PgDistanceType distanceType;
    private final int dimensions;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    private static final double SIMILARITY_THRESHOLD = 0.98; // 유사도 임계값, 필요에 따라 조정 가능

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
        logger.info("CustomPgVectorStore initialized with tableName: {}, dimensions: {}, distanceType: {}", tableName, dimensions, distanceType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Initializing CustomPgVectorStore for table: {}", tableName);
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        String createTableSql = String.format(
                "CREATE TABLE IF NOT EXISTS %s (" +
                        "id UUID PRIMARY KEY, " +
                        "content TEXT, " +
                        "metadata JSONB, " +
                        "embedding vector(%d), " +
                        "content_hash TEXT)",
                tableName, dimensions);
        jdbcTemplate.execute(createTableSql);
        String createIndexSql = String.format(
                "CREATE INDEX IF NOT EXISTS hnsw_%s_embedding ON %s USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 200)",
                tableName, tableName
        );
        jdbcTemplate.execute(createIndexSql);
        logger.info("CustomPgVectorStore initialization completed for table: {}", tableName);
    }

    private boolean isDuplicateOrSimilar(Document document) {
        String contentHash = calculateContentHash(document.getContent());
        if (isDocumentExists(contentHash)) {
            logger.debug("Duplicate document found: {}", document.getContent());
            return true;
        }
        List<Double> embedding = embeddingModel.embed(document.getContent());
        boolean isSimilar = isDocumentSimilar(embedding);
        if (isSimilar) {
            logger.debug("Similar document found: {}", document.getContent());
        }
        return isSimilar;
    }

    public boolean isDocumentSimilar(List<Double> newEmbedding) {
        String sql = String.format(
                "SELECT content, 1 - (embedding <=> ?) as similarity " +
                        "FROM %s " +
                        "ORDER BY embedding <=> ? " +
                        "LIMIT 1", tableName);

        return jdbcTemplate.query(
                sql,
                ps -> {
                    Array array = ps.getConnection().createArrayOf("float8", newEmbedding.toArray());
                    ps.setArray(1, array);
                    ps.setArray(2, array);
                },
                rs -> {
                    if (rs.next()) {
                        String existingContent = rs.getString("content");
                        double similarity = rs.getDouble("similarity");
                        boolean isSimilar = similarity > SIMILARITY_THRESHOLD;
                        logger.debug("Document similarity check result: {}, similarity score: {}", isSimilar, similarity);
                        logger.debug("Most similar existing content: '{}'", existingContent);
                        return isSimilar;
                    }
                    return false;
                }
        );
    }

    public boolean isDocumentExists(String contentHash) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE content_hash = ?", tableName);
        logger.debug("Executing SQL query to check document existence: {}", sql);
        logger.debug("Content hash: {}", contentHash);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, contentHash);
        boolean exists = count != null && count > 0;
        logger.debug("Document exists: {}", exists);
        return exists;
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Transactional
    @Override
    public void add(List<Document> documents) {
        try {
            List<Document> nonDuplicateDocuments = documents.stream()
                    .flatMap(doc -> splitDocument(doc).stream())
                    .filter(doc -> !isDuplicateOrSimilar(doc))
                    .collect(Collectors.toList());

            if (nonDuplicateDocuments.isEmpty()) {
                logger.info("No new documents to add. All documents are similar to existing ones.");
                return;
            }

            int insertedCount = batchInsert(nonDuplicateDocuments);
            logger.info("Successfully inserted {} non-duplicate documents into table: {}", insertedCount, tableName);
        } catch (Exception e) {
            logger.error("Error adding documents", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    private List<Document> splitDocument(Document document) {
        String[] lines = document.getContent().split("\n");
        return Arrays.stream(lines)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> new Document(line.trim(), document.getMetadata()))
                .collect(Collectors.toList());
    }

    private int batchInsert(List<Document> documents) {
        String sql = String.format("INSERT INTO %s (id, content, metadata, embedding, content_hash) VALUES (?, ?, ?::json, ?, ?)", tableName);

        int[] updateCounts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Document document = documents.get(i);
                List<Double> embedding = embeddingModel.embed(document.getContent());
                String contentHash = calculateContentHash(document.getContent());

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, document.getContent());
                try {
                    ps.setString(3, objectMapper.writeValueAsString(document.getMetadata()));
                } catch (JsonProcessingException e) {
                    throw new SQLException("Error converting metadata to JSON", e);
                }
                ps.setArray(4, ps.getConnection().createArrayOf("float8", embedding.toArray()));
                ps.setString(5, contentHash);
            }

            @Override
            public int getBatchSize() {
                return documents.size();
            }
        });

        return Arrays.stream(updateCounts).sum();
    }

    private String calculateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(hash);
            logger.debug("Calculated content hash: {}", result);
            return result;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating content hash", e);
            throw new RuntimeException("Error calculating content hash", e);
        }
    }

    @Override
    @Cacheable(value = "similaritySearchCache", key = "#request.query")
    public List<Document> similaritySearch(SearchRequest request) {
        logger.info("Performing similarity search for query: {}", request.getQuery());
        List<Double> queryEmbedding = embeddingModel.embed(request.getQuery());
        String sql = String.format(
                "SELECT id, content, metadata, embedding <-> CAST(? AS vector) as distance " +
                        "FROM %s " +
                        "ORDER BY embedding <-> CAST(? AS vector) " +
                        "LIMIT ?",
                tableName);

        List<Document> results = jdbcTemplate.query(sql, new Object[]{
                queryEmbedding.toArray(new Double[0]),
                queryEmbedding.toArray(new Double[0]),
                request.getTopK()
        }, new RowMapper<Document>() {
            @Override
            public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
                UUID id = UUID.fromString(rs.getString("id"));
                String content = rs.getString("content");
                String metadataJson = rs.getString("metadata");
                Map<String, Object> metadata = parseMetadata(metadataJson);
                logger.debug("Found similar document: id={}, content={}", id, content.substring(0, Math.min(content.length(), 50)));
                return new Document(id.toString(), content, metadata);
            }
        });

        logger.info("Similarity search completed. Found {} results.", results.size());
        return results;
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error parsing metadata JSON: {}", metadataJson, e);
            throw new RuntimeException("Error parsing metadata JSON", e);
        }
    }

    public CompletableFuture<Void> addAsync(List<Document> documents) {
        return CompletableFuture.runAsync(() -> {
            try {
                add(documents);
            } catch (Exception e) {
                logger.error("Error during async document addition", e);
                throw new CompletionException(e);
            }
        });
    }
}