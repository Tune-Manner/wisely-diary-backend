package tuneandmanner.wiselydiarybackend.common.rag.letter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LetterAssistantCommand {

    // 로깅을 위한 Logger 인스턴스 생성
    private static final Logger log = LoggerFactory.getLogger(LetterAssistantCommand.class);

    private final ChatModel chatModel;
    private final PgVectorStore letterVectorStore;
    private final PromptTemplate promptTemplate;

    // 생성자: 필요한 의존성 주입 및 프롬프트 템플릿 초기화
    public LetterAssistantCommand(ChatModel chatModel, PgVectorStore vectorStore,
                                  @Value("classpath:/prompts/letter-reference.st") Resource letterPromptTemplate) {
        this.chatModel = chatModel;
        this.letterVectorStore = vectorStore;
        // 프롬프트 템플릿 파일을 읽어 PromptTemplate 객체 생성
        this.promptTemplate = new PromptTemplate(letterPromptTemplate);
    }

    // 입력 메시지를 기반으로 편지 내용 생성
    public String generateLetterContents(String inputMessage) {
        log.info("입력을 기반으로 편지 생성 시작: {}", inputMessage);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", inputMessage);
        // 유사한 문서들을 찾아 프롬프트에 추가
        promptParameters.put("documents", String.join("\n", findSimilarDocuments(inputMessage)));

        try {
            // ChatModel을 사용하여 편지 내용 생성
            String generatedContent = chatModel.call(promptTemplate.create(promptParameters))
                    .getResult()
                    .getOutput()
                    .getContent();
            log.info("편지 생성 완료");
            return generatedContent;
        } catch (Exception e) {
            // 편지 생성 실패 시 로그 기록 및 예외 발생
            log.error("편지 생성 실패", e);
            throw new RuntimeException("편지 생성 실패", e);
        }
    }

    // 입력 메시지와 유사한 문서들을 벡터 스토어에서 검색
    private List<String> findSimilarDocuments(String message) {
        log.debug("메시지와 유사한 문서 검색 중: {}", message);
        // VectorStore를 사용하여 유사한 문서 3개 검색
        List<Document> similarDocuments = letterVectorStore.similaritySearch(SearchRequest.query(message).withTopK(3));
        // 검색된 문서의 내용만 추출하여 리스트로 반환
        return similarDocuments.stream().map(Document::getContent).toList();
    }
}
