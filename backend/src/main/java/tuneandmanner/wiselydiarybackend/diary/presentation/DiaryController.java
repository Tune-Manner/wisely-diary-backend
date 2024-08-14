package tuneandmanner.wiselydiarybackend.diary.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import tuneandmanner.wiselydiarybackend.diary.dto.request.DiaryDetailRequest;
import tuneandmanner.wiselydiarybackend.diary.dto.response.DiaryDetailResponse;
import tuneandmanner.wiselydiarybackend.diary.service.DiaryService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping("/diary/selectdetail")
    public ResponseEntity<DiaryDetailResponse> selectDetailDiary(@RequestBody DiaryDetailRequest request) {
        log.info("DiaryController.selectDetailDiary");

        DiaryDetailResponse response = diaryService.getDiaryContents(request);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
            .body(response);
    }


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

