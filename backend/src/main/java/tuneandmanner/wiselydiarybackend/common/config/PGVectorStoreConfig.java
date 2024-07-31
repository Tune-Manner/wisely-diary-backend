package tuneandmanner.wiselydiarybackend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PGVectorStoreConfig {

    @Value("${vector.store.embedding.dimension:1536}")
    private int embeddingDimension;

    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return new PgVectorStore(jdbcTemplate, embeddingModel, embeddingDimension);
    }
}
