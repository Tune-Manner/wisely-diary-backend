package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAiConfig;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;
import tuneandmanner.wiselydiarybackend.member.domain.entity.Member;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class DalleApiService {

    private final OpenAiConfig openAiConfig;
    private final RestTemplate restTemplate;

    private static final String DALLE_API_URL = "https://api.openai.com/v1/images/generations";


    public String generateCartoonPrompt(Emotion emotion,Member member, String prompt) {
        log.info("DalleApiService.generateCartoonPrompt");
        LocalDate birthDate = member.getMemberAge(); // 생년월일
        LocalDate currentDate = LocalDate.now(); // 현재 날짜
        int age = Period.between(birthDate, currentDate).getYears();
        System.out.println("나이는??"+age);

        String gender = member.getMemberGender();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        String emotionContents = null;
        switch (emotion.getEmotionCode()){
            case 1:
                emotionContents = "걱정";
                break;
            case 2:
                emotionContents = "뿌듯";
                break;
            case 3:
                emotionContents = "감사";
                break;
            case 4:
                emotionContents = "억울";
                break;
            case 5:
                emotionContents = "분노";
                break;
            case 6:
                emotionContents = "슬픔";
                break;
            case 7:
                emotionContents = "설렘";
                break;
            case 8:
                emotionContents = "신남";
                break;
            case 9:
                emotionContents = "편안";
                break;
            case 10:
                emotionContents = "당황";
        }
        System.out.println("오늘의기분은???"+emotionContents);
        String refinedPrompt = "페르소나 : " + age + "세 " + gender + "\n" +
                "감정: " + emotionContents + "\n" +
                "\n" +
                "'" + prompt + "'" +
                "\n" +
                "\n" +
                "오늘 일기를 한 문장으로 요약해줘. \n" +
                "그 문장을 Minimalist Style 일러스트로 만들어줘. 텍스트 없이 만들어줘.";

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

    public String generateImage(String prompt) {
        log.info("DalleApiService.generateImage 시작");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiConfig.getApiKey());

        // DALL-E 3의 최대 문자 제한에 맞춰 프롬프트 자르기
        String refinedPrompt = truncatePrompt(prompt, 1000);

        log.info("정제된 프롬프트: {}", refinedPrompt);

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
                String imageUrl = extractImageUrlFromResponse(response.getBody());
                log.info("생성된 이미지 URL: {}", imageUrl);
                return imageUrl;
            }
            log.error("이미지 생성 실패. 응답: {}", response);
            throw new RuntimeException("DALL-E API에서 유효하지 않은 응답을 받아 이미지 생성에 실패했습니다.");
        } catch (Exception e) {
            log.error("DALL-E API 호출 중 오류 발생", e);
            throw new RuntimeException("DALL-E API 호출 실패", e);
        }
    }
}
