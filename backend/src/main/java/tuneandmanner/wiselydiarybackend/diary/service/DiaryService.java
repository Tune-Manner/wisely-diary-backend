package tuneandmanner.wiselydiarybackend.diary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.rag.service.RAGService;

@Service
public class DiaryService {

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private RAGService ragService;

    /**
     * 일기 코드를 기반으로 편지를 생성합니다.
     * @param diaryCode 일기 코드
     * @return 생성된 편지
     */
    public String generateLetter(Long diaryCode) {
        Diary diary = diaryRepository.findById(diaryCode)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));

        String query = "이 일기 내용을 바탕으로 따뜻한 위로와 격려의 편지를 작성해주세요.";
        String context = String.format("일기 내용: %s\n감정 코드: %d",
                diary.getDiaryContents(),
                diary.getEmotionCode());

        return ragService.generateResponse(query, context, "letter");
    }
}
