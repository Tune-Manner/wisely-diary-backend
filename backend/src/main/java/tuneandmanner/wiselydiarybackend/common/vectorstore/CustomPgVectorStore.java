package tuneandmanner.wiselydiarybackend.common.vectorstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (createIndexMethod != PgIndexType.NONE) {
            jdbcTemplate.execute(String.format(
                    "CREATE INDEX IF NOT EXISTS %s ON %s USING %s (embedding %s)",
                    "spring_ai_vector_index_" + tableName, tableName, createIndexMethod, distanceType.index));
        }
    }

    @Override
    public void add(List<Document> documents) {
        String sql = String.format("INSERT INTO %s (id, content, metadata, embedding) VALUES (?, ?, ?::json, ?)", tableName);
        logger.info("Executing batch insert into table: {}", tableName);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Document document = documents.get(i);
                List<Double> embedding = embeddingModel.embed(document.getContent());

                // 로깅 추가
                logger.info("Document {}: Content '{}', Embedding dimension: {}, Expected dimension: {}",
                        i, document.getContent(), embedding.size(), dimensions);
                logger.info("Document {}: Content '{}', Embedding dimension: {}", i, document.getContent(), embedding.size());

                if (embedding.size() != dimensions) {
                    logger.error("Embedding dimension mismatch. Expected: {}, Actual: {}", dimensions, embedding.size());
                    throw new IllegalStateException("Embedding dimension mismatch");
                }

                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, document.getContent());
                try {
                    ps.setString(3, objectMapper.writeValueAsString(document.getMetadata()));
                } catch (JsonProcessingException e) {
                    throw new SQLException("Error converting metadata to JSON", e);
                }
                ps.setArray(4, ps.getConnection().createArrayOf("float4", embedding.toArray()));
            }

            @Override
            public int getBatchSize() {
                return documents.size();
            }
        });

        logger.info("Successfully inserted {} documents into table: {}", documents.size(), tableName);
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

        return jdbcTemplate.query(sql, new Object[]{
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
                return new Document(id.toString(), content, metadata);
            }
        });
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