package tuneandmanner.wiselydiarybackend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Configuration
public class PromptTemplateConfig {

    @Bean
    public PromptTemplate ragPromptTemplate() {
        String template = """
        You are an AI assistant specialized in using Retrieval-Augmented Generation (RAG) to provide accurate and context-aware responses.
        
        Context information:
        {context}
        
        User query: {query}
        
        Based on the given context and the user's query, please provide a detailed and relevant response.
        If the context doesn't contain enough information to answer the query, please state that clearly.
        """;
        return new PromptTemplate(template);
    }
}