package tuneandmanner.wiselydiarybackend.diary.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.diary.dto.request.DiaryDetailRequest;
import tuneandmanner.wiselydiarybackend.diary.dto.request.ModifyDiaryContentRequestDTO;
import tuneandmanner.wiselydiarybackend.diary.dto.response.DiaryDetailResponse;
import tuneandmanner.wiselydiarybackend.diary.dto.response.ModifyContentResponseDTO;
import tuneandmanner.wiselydiarybackend.diarysummary.domain.DiarySummary;
import tuneandmanner.wiselydiarybackend.diarysummary.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.rag.service.RAGService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Slf4j
@Service
public class DiaryService {

    private static final Logger logger = LoggerFactory.getLogger(DiaryService.class);

    private final ModelMapper modelMapper;
    private final DiaryRepository diaryRepository;
    private final DiarySummaryRepository diarySummaryRepository;
    private final RAGService ragService;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.urls.base-url}")
    private String openAiBaseUrl;

    @Autowired
    public DiaryService(ModelMapper modelMapper, DiaryRepository diaryRepository, DiarySummaryRepository diarySummaryRepository, RAGService ragService) {
		this.modelMapper = modelMapper;
		this.diaryRepository = diaryRepository;
        this.diarySummaryRepository = diarySummaryRepository;
        this.ragService = ragService;
    }

//    /**
//     * 일기 코드를 기반으로 편지를 생성합니다.
//     * @param diaryCode 일기 코드
//     * @return 생성된 편지
//     */
//    public Map<String, String> generateLetter(Long diaryCode) {
//        Diary diary = diaryRepository.findById(diaryCode)
//                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
//
//        String query = "이 일기 내용을 바탕으로 따뜻한 위로와 격려의 편지를 한국어로 작성해주세요.";
//        String context = String.format("일기 내용: %s\n감정 코드: %d",
//                diary.getDiaryContents(),
//                diary.getEmotionCode());
//
//        return ragService.generateResponse(query, context, "letter");
//    }

    /**
     * 일기를 요약하고 저장합니다.
     * @param diaryCode 요약할 일기의 코드
     * @return 생성된 요약 내용
     */
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public String summarizeDiary(Long diaryCode) {
        logger.info("Summarizing diary with code: {}", diaryCode);

        try {
            logger.debug("Fetching diary from repository");
            Diary diary = diaryRepository.findById((long) Math.toIntExact(diaryCode))
                    .orElseThrow(() -> {
                        logger.error("Diary not found with code: {}", diaryCode);
                        return new RuntimeException("일기를 찾을 수 없습니다.");
                    });

            logger.debug("Generating summary for diary");
            String summary = generateSummary(diary);
            logger.debug("Summary generated: {}", summary);

            logger.debug("Checking for existing summary");
            DiarySummary existingSummary = diarySummaryRepository.findByDiaryCode(diaryCode);
            if (existingSummary != null) {
                logger.debug("Updating existing summary");
                DiarySummary updatedSummary = existingSummary.updateContents(summary);
                diarySummaryRepository.save(updatedSummary);
            } else {
                logger.debug("Creating new summary");
                DiarySummary newSummary = DiarySummary.builder()
                        .diarySummaryContents(summary)
                        .diaryCode(diaryCode)
                        .build();
                diarySummaryRepository.save(newSummary);
            }

            logger.info("Diary summary process completed successfully for diary code: {}", diaryCode);

            entityManager.flush();
            entityManager.clear();

            return summary;
        } catch (Exception e) {
            logger.error("Error occurred while summarizing diary with code: {}", diaryCode, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw e;
        }
    }

    private String generateSummary(Diary diary) {
        logger.debug("Generating summary for diary content: {}", diary.getDiaryContents());
        String query = "이 일기 내용을 명확한 기승전결이 있는 4개의 문장으로 요약해주세요. 요약문은 한국어로 작성되어야 합니다. "
                + "Beautify된 Json 형태의 반환 양식을 무조건 지켜 주세요."
                + " 예시: {\\n\\t\"response\": [\\n\\t\\t\"1번째 문장\", \\n\\t\\t\"2번째 문장\", \\n\\t\\t\"3번째 문장\", \\n\\t\\t\"4번째 문장\"\\n\\t]\\n}";
        String context = diary.getDiaryContents();
        Map<String, String> result = ragService.generateResponse(query, context, "summary");
        logger.debug("Summary generated by RAG service: {}", result.get("response"));
        return result.get("response");
    }

    // 선택한 날짜의 일기 하나 가져오기
    public DiaryDetailResponse getDiaryContents(DiaryDetailRequest request) {
        log.info("DiaryService.getDiaryContents - memberId: {}, date: {}", request.getMemberId(), request.getDate());

        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return diaryRepository.findByMemberIdAndCreatedAtBetween(request.getMemberId(), startOfDay, endOfDay)
            .map(diary -> new DiaryDetailResponse(
                diary.getDiaryCode(), // 추가된 diaryCode
                diary.getCreatedAt().toLocalDate().toString(),
                diary.getDiaryContents()
            ))
            .orElse(new DiaryDetailResponse(
                null, // 일기를 찾을 수 없는 경우 diaryCode를 null로 설정
                request.getDate(),
                "해당 날짜의 일기를 찾을 수 없습니다."));
    }

    // 선택한 달의 일기 내용들 가져오기
    public List<DiaryDetailResponse> getDiaryContentsbyMonth(DiaryDetailRequest request) {
        log.info("DiaryService.getDiaryContentsbyMonth - memberId: {}, date: {}", request.getMemberId(), request.getDate());

        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);

        List<Diary> diaries = diaryRepository.findByMemberIdAndCreatedAtBetweenAndDiaryStatusOrderByCreatedAtDesc(
            request.getMemberId(), startOfMonth, endOfMonth, "EXIST");

        return diaries.stream()
            .map(diary -> new DiaryDetailResponse(
                diary.getDiaryCode(), // 추가된 diaryCode
                diary.getCreatedAt().toLocalDate().toString(),
                diary.getDiaryContents()))
            .collect(Collectors.toList());
    }

    public String generateDiaryEntry(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 JSON 생성
        JsonObject requestJson = new JsonObject();

        requestJson.addProperty("model", "gpt-3.5-turbo");
        requestJson.addProperty("temperature", 0.7); // 텍스트 생성의 랜덤성을 조절하는 파라미터

        // 메시지 배열 생성
        JsonArray messagesArray = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);
        messagesArray.add(userMessage);

        requestJson.add("messages", messagesArray);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);


        HttpEntity<String> entity = new HttpEntity<>(requestJson.toString(), headers);

        String API_URL = openAiBaseUrl + "/chat/completions";

        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonObject responseJson = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
        } else {
            throw new RuntimeException("Failed to generate diary entry");
        }
    }

    public Long saveDiaryEntry(String diaryContent, String memberId, int emotionCode) {
        Diary diary = Diary.builder()
            .diaryContents(diaryContent)
            .memberId(memberId)
            .createdAt(LocalDateTime.now())
            .emotionCode(emotionCode)
            .diaryStatus("EXIST")
            .build();

        Diary savedDiary = diaryRepository.save(diary);
        return savedDiary.getDiaryCode();  // 생성된 diaryCode를 반환
    }

    @Transactional
    public ModifyContentResponseDTO modifyDiaryContent(ModifyDiaryContentRequestDTO modifyDiaryContentRequestDTO) {
        // 다이어리 조회
        Diary diary = diaryRepository.findByDiaryCode(modifyDiaryContentRequestDTO.getDiaryCode())
            .orElseThrow(() -> new EntityNotFoundException("exception.data.entityNotFound"));

        // 다이어리 내용 업데이트
        diary.setDiaryContents(modifyDiaryContentRequestDTO.getDiaryContent());
        Diary updatedDiary = diaryRepository.save(diary);

        // 수정된 내용을 DTO로 변환하여 반환
        return modelMapper.map(updatedDiary, ModifyContentResponseDTO.class);
    }
}
