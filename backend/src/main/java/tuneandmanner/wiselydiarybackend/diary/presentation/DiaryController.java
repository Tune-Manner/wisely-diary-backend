package tuneandmanner.wiselydiarybackend.diary.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.diary.service.DiaryService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

//    @GetMapping("/rag/letter/{diaryCode}")
//    public Map<String, String> generateLetter(@PathVariable Long diaryCode) {
//        return diaryService.generateLetter(diaryCode);
//    }

    /**
     * 일기를 요약하고 저장하는 엔드포인트
     * @param diaryCode 요약할 일기의 코드
     * @return 생성된 요약 내용
     */
    @GetMapping("/rag/summarize/{diaryCode}")
    public ResponseEntity<String> summarizeDiary(@PathVariable Long diaryCode) {
        String summary = diaryService.summarizeDiary(diaryCode);
        return ResponseEntity.ok(summary);
    }
}