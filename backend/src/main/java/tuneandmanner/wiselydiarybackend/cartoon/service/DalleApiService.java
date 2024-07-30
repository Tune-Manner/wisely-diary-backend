package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import tuneandmanner.wiselydiarybackend.auth.config.DalleConfig;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DalleApiService {

    private final DalleConfig dalleConfig;

    private static final String DALLE_API_URL = "https://api.openai.com/v1/images/generations";

    public String generateCartoonPrompt(String diarySummary) {
        log.info("DalleApiService.generateCartoonPrompt");

        String apiKey = dalleConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("DALL-E API Key is not set");
            throw new IllegalStateException("DALL-E API Key is not configured");
        }

        log.info("Using API Key: {}", apiKey.substring(0, Math.min(apiKey.length(), 5)) + "...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String prompt = String.format("Create a single image with 4 comic-style panels arranged in a 2x2 grid, " +
                "illustrating the following diary summary: '%s'. " +
                "Use simple line drawings for all panels.", diarySummary);

        Map<String, Object> requestBody = Map.of(
                "prompt", prompt,
                "n", 1,
                "size", "1024x1024"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map<String, Object>> response = dalleConfig.restTemplate().exchange(
                    DALLE_API_URL,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );
            log.info("DALL-E API Response: {}", response);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object responseData = response.getBody().get("data");
                if (responseData instanceof List<?> dataList) {
                    if (!dataList.isEmpty() && dataList.get(0) instanceof Map<?, ?> dataMap) {
                        Object url = dataMap.get("url");
                        if (url instanceof String) {
                            return (String) url;
                        }
                    }
                }
            }
            log.error("Failed to generate image. Response: {}", response);
            throw new RuntimeException("Failed to generate image. Invalid response from DALL-E API.");
        } catch (RestClientException e) {
            log.error("Error while calling DALL-E API", e);
            throw new RuntimeException("Failed to call DALL-E API", e);
        }
    }
}
