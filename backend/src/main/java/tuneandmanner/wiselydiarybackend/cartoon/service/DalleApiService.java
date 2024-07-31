package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAiConfig;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DalleApiService {

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;

    private static final String DALLE_API_URL = "https://api.openai.com/v1/images/generations";


    public String generateCartoonPrompt(String prompt) {
        log.info("DalleApiService.generateCartoonPrompt");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        String refinedPrompt = prompt;

        refinedPrompt = truncatePrompt(refinedPrompt, 1000);  // DALL-E 3 has a 4000 character limit

        log.info("여기 확인하기 :" + refinedPrompt);

        Map<String, Object> requestBody = Map.of(
                "model", openAiConfig.getImageModel(),
                "prompt", refinedPrompt,
                "n", 1,
                "size", openAiConfig.getImageSize(),
                "quality", openAiConfig.getImageQuality(),
                "style", openAiConfig.getImageStyle()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    DALLE_API_URL,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractImageUrlFromResponse(response.getBody());
            }
            log.error("Failed to generate image. Response: {}", response);
            throw new RuntimeException("Failed to generate image. Invalid response from DALL-E API.");
        } catch (Exception e) {
            log.error("Error while calling DALL-E API", e);
            throw new RuntimeException("Failed to call DALL-E API", e);
        }
    }

    private String extractImageUrlFromResponse(Map responseBody) {
        if (responseBody.containsKey("data")) {
            List<Map<String, String>> dataList = (List<Map<String, String>>) responseBody.get("data");
            if (!dataList.isEmpty()) {
                return dataList.get(0).get("url");
            }
        }
        throw new RuntimeException("Failed to extract image URL from DALL-E response");
    }

    private String truncatePrompt(String prompt, int maxLength) {
        if (prompt.length() <= maxLength) {
            return prompt;
        }
        return prompt.substring(0, maxLength - 3) + "...";
    }
}
