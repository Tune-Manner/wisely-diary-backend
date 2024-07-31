package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAiConfig;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.DiarySummary;
import tuneandmanner.wiselydiarybackend.cartoon.domain.repository.DiarySummaryRepository;

import java.util.List;
import java.util.Map;

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

        String systemMessage = "You are an expert in creating prompts for diverse comic styles from around the world. " +
                "Always follow these guidelines:\n" +
                "1. Create exactly 4 panels in a 2x2 grid layout.\n" +
                "2. Use simple black lines on a white background.\n" +
                "3. Focus on the main character (a 19-year-old woman) and her experiences.\n" +
                "4. Accurately reflect the events and emotions described in the diary.\n" +
                "5. If celebrities or specific individuals are mentioned, depict them accordingly, not the main character.\n" +
                "6. Do not add extra scenes or details not mentioned in the diary.\n" +
                "7. Do not use text or speech bubbles.\n" +
                "Always format your response as: 'Create a single image with 4 comic panels in a 2x2 grid layout, using a [specific style] comic approach. [Detailed description of each panel here]'";

        String userPrompt = "Based on this diary entry, create a prompt for a 4-panel comic in an appropriate cultural style: \"" + diarySummary + "\"";



        Map<String, Object> requestBody = Map.of(
                "model", openAiConfig.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.5,
                "max_tokens", 500
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