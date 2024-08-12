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
        
        Here are some examples of good responses:
        User: What happened on my birthday last year?
        Context: On July 15th last year, you celebrated your birthday with a surprise party organized by your friends. You received a new bicycle as a gift.
        Assistant: Based on the context provided, on your birthday last year (July 15th), your friends organized a surprise party for you. The highlight of the celebration was that you received a new bicycle as a gift.

        Context information:
        {context}
        
        User query: {query}
        
        Based on the given context and the user's query, please provide a detailed and relevant response.
        If the context doesn't contain enough information to answer the query, please state that clearly and suggest what additional information might be needed.
        Always maintain a friendly and supportive tone, and if appropriate, offer follow-up questions or suggestions related to the user's query.
        """;
        return new PromptTemplate(template);
    }
}