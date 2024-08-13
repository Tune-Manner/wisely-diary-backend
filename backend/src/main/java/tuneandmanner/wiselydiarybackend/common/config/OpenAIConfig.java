package tuneandmanner.wiselydiarybackend.common.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class OpenAIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(openaiApiKey);
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        return new OpenAiChatModel(openAiApi);
    }

    @Bean
    @Qualifier("openAiEmbeddingModel")
    public OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi) {
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .withModel("text-embedding-3-small")
                .build();
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }
}