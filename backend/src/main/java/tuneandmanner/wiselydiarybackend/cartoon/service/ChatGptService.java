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

        String systemMessage = "당신은 웹툰 스타일의 만화를 위한 프롬프트를 만드는 전문가입니다." +
                "주어진 일기 항목을 바탕으로 4개의 문장을 작성하세요. 간결하지만 완전해야 합니다." +
                "다음 지침을 따르세요:\n" +
                "1.흰색 배경에 검은색 선이 있는 단순하고 깔끔한 선화 스타일을 사용합니다.\n" +
                "2.캐릭터와 최소한의 배경 요소에만 파스텔 색상의 작은 팝을 추가합니다.\n" +
                "3. 일기장의 사건과 감정을 정확하게 반영합니다.\n" +
                "4. 유명인이나 아이돌은 친구가 아닌 공적인 인물로 묘사합니다.\n" +
                "5. 텍스트나 말풍선을 사용하지 않습니다.\n" +
                "6. 모든 캐릭터를 전형적인 한국인의 이목구비와 헤어스타일로 묘사하세요.\n" +
                "응답 형식을 다음과 같이 지정합니다: " +
//                "'Create a single image with 4 webtoon-style comic panels in a 2x2 grid:\n" +
                "첫 번째 컷: 주인공이  [Brief description],\n두 번째 컷: 그 이후에는 주인공이  [Brief description],\n세 번째 컷: 그 이후에는 주인공이  [Brief description],\n네 번째 컷: 그 이후에는 주인공이  [Brief description]'" +
                "각 컷에 대해 어떻게 그릴지 상세하게 표현해주세요."+
                "첫 번째 컷에서 네 번째 컷에 요약된 내용을 한국어로 번역해주세요."+
                "프롬프트를 영어로 번역해서 나타내주세요." ;

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