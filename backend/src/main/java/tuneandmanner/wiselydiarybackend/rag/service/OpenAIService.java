package tuneandmanner.wiselydiarybackend.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import java.util.Map;
import java.util.HashMap;

@Service
public class OpenAIService {

    private final OpenAiChatModel chatModel;

    public OpenAIService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * OpenAI API를 사용하여 주어진 프롬프트에 대한 응답을 생성합니다.
     * @param prompt 생성된 프롬프트
     * @return 생성된 응답
     */
    public String generateResponse(Prompt prompt) {
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getContent();
    }
}
