package tuneandmanner.wiselydiarybackend.letter.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.letter.dto.request.CreateLetterRequest;
import tuneandmanner.wiselydiarybackend.letter.dto.response.CreateLetterResponse;
import tuneandmanner.wiselydiarybackend.letter.service.LetterService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @PostMapping("/{diarySummaryCode}")
    public ResponseEntity<Map<String, Object>> createLetter(@RequestBody @Valid CreateLetterRequest request) {
        CreateLetterResponse response = letterService.createLetter(request.getDiarySummaryCode());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("letterCode", response.getLetterCode());
        responseMap.put("diarySummaryCode", response.getDiarySummaryCode());
        responseMap.put("letterContents", response.getLetterContents());
        responseMap.put("createdAt", response.getCreatedAt().toString());  // 문자열로 변환

        return ResponseEntity.created(URI.create("/api/letter/" + response.getLetterCode()))
                .body(responseMap);
    }
//    public ResponseEntity<CreateLetterResponse> createLetter(@RequestBody @Valid CreateLetterRequest request) {
//        CreateLetterResponse response = letterService.createLetter(request.getDiarySummaryCode());
//        return ResponseEntity.created(URI.create("/api/letter/" + response.getLetterCode()))
//                .body(response);
//    }

    @GetMapping("/{letterCode}")
    public ResponseEntity<CreateLetterResponse> getLetter(
            @PathVariable Long letterCode
    ) {
        CreateLetterResponse response = letterService.getLetter(letterCode);
        return ResponseEntity.ok(response);
    }
}