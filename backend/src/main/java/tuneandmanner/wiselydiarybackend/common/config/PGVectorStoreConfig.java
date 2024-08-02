package tuneandmanner.wiselydiarybackend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import tuneandmanner.wiselydiarybackend.common.vectorstore.CustomPgVectorStore;

import java.util.List;
import java.util.Map;

@Configuration
public class PGVectorStoreConfig {
    private static final Logger logger = LoggerFactory.getLogger(PGVectorStoreConfig.class);

    @Value("${vector.store.embedding.dimension:1536}")
    private int embeddingDimension;

    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializePgVector() {
        logger.info("Attempting to initialize pgvector extension");
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            logger.info("pgvector extension initialized successfully");

            // 확장 설치 확인
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM pg_extension WHERE extname = 'vector'");
            if (!result.isEmpty()) {
                logger.info("Confirmed pgvector extension is installed: {}", result);
            } else {
                logger.warn("pgvector extension not found in pg_extension");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize pgvector extension", e);
            throw new RuntimeException("Application startup failed due to pgvector initialization error", e);
        }
    }

    @Bean
    public PgVectorStore pgVectorStoreSummary(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return createCustomPgVectorStore(jdbcTemplate, embeddingModel, "vector_store_summary", objectMapper);
    }

    @Bean
    public PgVectorStore pgVectorStoreLetter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return createCustomPgVectorStore(jdbcTemplate, embeddingModel, "vector_store_letter", objectMapper);
    }

    @Bean
    public PgVectorStore pgVectorStoreMusic(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return createCustomPgVectorStore(jdbcTemplate, embeddingModel, "vector_store_music", objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private CustomPgVectorStore createCustomPgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, String tableName, ObjectMapper objectMapper) {
        logger.info("Creating CustomPgVectorStore for table: {}", tableName);
        CustomPgVectorStore store = new CustomPgVectorStore(
                jdbcTemplate,
                embeddingModel,
                embeddingDimension,
                PgVectorStore.PgDistanceType.COSINE_DISTANCE,
                false,  // removeExistingVectorStoreTable
                PgVectorStore.PgIndexType.HNSW,  // HNSW 인덱스 사용
                true,  // initializeSchema
                tableName,
                objectMapper
        );
        logger.info("CustomPgVectorStore created successfully for table: {}", tableName);
        return store;
    }
}