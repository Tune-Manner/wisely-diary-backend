package tuneandmanner.wiselydiarybackend.rag.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RAGService {

    private final VectorStoreService vectorStoreService;
    private final OpenAIService openAIService;
    private final PromptTemplate ragPromptTemplate;

    public RAGService(
            VectorStoreService vectorStoreService,
            OpenAIService openAIService,
            @Qualifier("ragPromptTemplate") PromptTemplate ragPromptTemplate
    ) {
        this.vectorStoreService = vectorStoreService;
        this.openAIService = openAIService;
        this.ragPromptTemplate = ragPromptTemplate;
    }

    /**
     * RAG 프로세스를 실행하여 주어진 쿼리에 대한 응답을 생성합니다.
     * @param query 사용자 쿼리
     * @param context 추가 컨텍스트 정보
     * @return 생성된 응답
     */
    public String generateResponse(String query, String context, String storeType) {
        // 벡터 저장소에서 유사한 문서 검색
        List<String> similarDocuments = vectorStoreService.searchSimilarDocuments(context, storeType);

        // 프롬프트 생성
        Prompt prompt = createPrompt(query, context, similarDocuments);

        // OpenAI에 프롬프트 전송 및 응답 생성
        return openAIService.generateResponse(prompt);
    }

    /**
     * 프롬프트를 생성합니다.
     */
    private Prompt createPrompt(String query, String context, List<String> similarDocuments) {
        String combinedContext = String.join("\n", similarDocuments) + "\n" + context;
        Map<String, Object> model = new HashMap<>();
        model.put("context", combinedContext);
        model.put("query", query);
        return ragPromptTemplate.create(model);
    }
}