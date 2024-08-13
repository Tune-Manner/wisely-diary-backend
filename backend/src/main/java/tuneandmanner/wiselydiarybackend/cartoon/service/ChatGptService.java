package tuneandmanner.wiselydiarybackend.cartoon.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAiConfig;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatGptService {

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;

    private static final String CHAT_GPT_API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateCartoonPrompt(String diarySummary) {
        log.info("ChatGptService.generateCartoonPrompt");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        String systemMessage = "You are an expert in creating prompts for webtoon-style comics. " +
                "Create 4 panel descriptions based on the given diary entry. Be concise but complete. " +
                "Follow these guidelines:\n" +
                "1. Use a simple, clean line art style with black lines on a white background.\n" +
                "2. Add small pops of pastel color only to characters and minimal background elements.\n" +
                "3. Accurately reflect events and emotions from the diary.\n" +
                "4. Depict celebrities or idols as public figures, not friends.\n" +
                "5. Do not use text or speech bubbles.\n" +
                "6. Depict all characters with typical Korean features and hairstyles.\n" +
                "Format your response as: " +
//                "'Create a single image with 4 webtoon-style comic panels in a 2x2 grid:\n" +
                "P1: [Brief description]\nP2: [Brief description]\nP3: [Brief description]\nP4: [Brief description]'"+
                "P1~P4에 요약된 내용을 한국어로 번역해줘";

        String userPrompt = "Based on this diary entry, create a prompt for a 4-panel webtoon-style comic: \"" + diarySummary + "\"";

        Map<String, Object> requestBody = Map.of(
                "model", openAiConfig.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.5,
                "max_tokens", 1000
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    CHAT_GPT_API_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("여기확인1: "+ extractPromptFromResponse(response.getBody()));
                return extractPromptFromResponse(response.getBody());
            }

            log.error("Failed to generate prompt. Response: {}", response);
            throw new RuntimeException("Failed to generate prompt. Invalid response from ChatGPT API.");
        } catch (Exception e) {
            log.error("Error while calling ChatGPT API", e);
            throw new RuntimeException("Failed to call ChatGPT API", e);
        }
    }

    private String extractPromptFromResponse(Map responseBody) {
        if (responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                if (choice.containsKey("message")) {
                    Map<String, String> message = (Map<String, String>) choice.get("message");
                    String content = message.get("content");
                    return truncatePrompt(content, 950);  // 프롬프트 길이를 950자로 제한
                }
            }
        }
        throw new RuntimeException("Failed to extract prompt from ChatGPT response");
    }

    // 프롬프트를 지정된 길이로 자르는 메서드
    private String truncatePrompt(String prompt, int maxLength) {
        if (prompt.length() <= maxLength) {
            return prompt;
        }
        return prompt.substring(0, maxLength - 3) + "...";
    }
}