package tuneandmanner.wiselydiarybackend.diary.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.diarysummary.domain.DiarySummary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.diarysummary.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.rag.service.RAGService;

import java.util.Map;

@Service
public class DiaryService {

    private static final Logger logger = LoggerFactory.getLogger(DiaryService.class);

    private final DiaryRepository diaryRepository;
    private final DiarySummaryRepository diarySummaryRepository;
    private final RAGService ragService;

    @Autowired
    public DiaryService(DiaryRepository diaryRepository, DiarySummaryRepository diarySummaryRepository, RAGService ragService) {
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
            Diary diary = diaryRepository.findById(diaryCode)
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
}
