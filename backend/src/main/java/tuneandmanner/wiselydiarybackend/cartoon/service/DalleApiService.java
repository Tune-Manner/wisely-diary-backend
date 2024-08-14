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

        String refinedPrompt =
//                "Create a single image with 4 webtoon-style comic panels in a 2x2 grid. " +
//                "Use simple, clean line art with black lines on a white background. " +
//                "Add small pops of pastel color only to characters and minimal background elements. " +
//                "Depict all characters with typical Korean features and hairstyles. " +
                "웹툰 스타일의 4컷 만화로 구성하여 하나의 이미지를 그려주세요. 깔끔한 선으로, " +
                        "흰색 배경에 검은 선을 사용하세요.배경은 단순하게 그려주고 캐릭터 요소에만 파스텔 색상으로 은은하게 색칠해주세요." +
                        "아래의 프롬프트대로 그려주세요."+
                        "모든 캐릭터는 현대의 한국인 특징과 헤어 스타일을 가지도록 그리세요." +
                        "그림을 그릴 때 컷마다 설명되지 않은 내용은 그리지 말아주세요." +
                        "이 만화의 주인공은 20대 한국인 남성입니다." +

                prompt;

        refinedPrompt = truncatePrompt(refinedPrompt, 1000);  // DALL-E 3 has a 4000 character limit

        log.info("여기 확인하기 :" + refinedPrompt);

        Map<String, Object> requestBody = Map.of(
                "model", openAiConfig.getImageModel(),
                "prompt", refinedPrompt,
                "n", 1,
                "size", openAiConfig.getImageSize(),
                "quality", openAiConfig.getImageQuality()

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
