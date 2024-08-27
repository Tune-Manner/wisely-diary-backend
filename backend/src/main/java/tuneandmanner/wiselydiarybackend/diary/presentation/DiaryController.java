package tuneandmanner.wiselydiarybackend.diary.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import tuneandmanner.wiselydiarybackend.diary.dto.request.DiaryDetailRequest;
import tuneandmanner.wiselydiarybackend.diary.dto.request.ModifyDiaryContentRequestDTO;
import tuneandmanner.wiselydiarybackend.diary.dto.response.DiaryDetailResponse;
import tuneandmanner.wiselydiarybackend.diary.dto.response.ModifyContentResponseDTO;
import tuneandmanner.wiselydiarybackend.diary.service.DiaryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    // 선택한 날짜의 일기 하나 가져오기
    @PostMapping("/diary/selectdetail")
    public ResponseEntity<DiaryDetailResponse> selectDetailDiary(@RequestBody DiaryDetailRequest request) {
        log.info("DiaryController.selectDetailDiary");

        DiaryDetailResponse response = diaryService.getDiaryContents(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .body(response);
    }

    // 선택한 달의 일기 내용들 가져오기
    @PostMapping("/diary/monthly")
    public ResponseEntity<List<DiaryDetailResponse>> monthlyDiary(@RequestBody DiaryDetailRequest request) {
        log.info("DiaryController.monthlyDiary");

        List<DiaryDetailResponse> responses = diaryService.getDiaryContentsbyMonth(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .body(responses);

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

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateDiary(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String memberId = request.get("memberId");
        String emotionCodeStr = request.get("emotionCode");

        if (prompt == null || memberId == null || emotionCodeStr == null) {
            log.error("Missing required parameters");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Missing required parameters");
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body((Map) errorResponse);
        }

        try {
            int emotionCode = Integer.parseInt(emotionCodeStr);
            String diaryEntry = diaryService.generateDiaryEntry(prompt);
            Long diaryCode = diaryService.saveDiaryEntry(diaryEntry, memberId, emotionCode);

            Map<String, Object> response = new HashMap<>();
            response.put("diaryEntry", diaryEntry);
            response.put("diaryCode", diaryCode);

            log.info("Successfully generated and saved diary entry with diaryCode: {}", diaryCode);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body(response);
        } catch (NumberFormatException e) {
            log.error("Invalid emotion code format", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid emotion code format");
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body((Map) errorResponse);
        } catch (Exception e) {
            log.error("Failed to generate diary entry", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate diary entry: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body((Map) errorResponse);
        }
    }

    @PutMapping("/modify/{diaryCode}")
    public ResponseEntity<ModifyContentResponseDTO> modifyDiaryContent(
            @PathVariable Long diaryCode,
            @RequestBody ModifyDiaryContentRequestDTO modifyDiaryContentRequestDTO) {

        // Request DTO에 다이어리 코드 설정
        modifyDiaryContentRequestDTO.setDiaryCode(diaryCode);

        // 서비스에서 일기 내용 수정 처리
        ModifyContentResponseDTO responseModifyContent = diaryService.modifyDiaryContent(modifyDiaryContentRequestDTO);

        // 수정된 일기 내용 응답
        if (responseModifyContent != null) {
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8)
                    .body(responseModifyContent);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body(null);
        }
    }
}

