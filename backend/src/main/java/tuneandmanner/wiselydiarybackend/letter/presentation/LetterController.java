package tuneandmanner.wiselydiarybackend.letter.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.letter.dto.response.CreateLetterResponse;
import tuneandmanner.wiselydiarybackend.letter.service.LetterService;

@Slf4j
@RestController
@RequestMapping("/api/letter")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @GetMapping("/{diaryCode}")
    public ResponseEntity<CreateLetterResponse> getOrCreateLetter(@PathVariable Long diaryCode) {
        CreateLetterResponse response = letterService.getOrCreateLetter(diaryCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view/{letterCode}")
    public ResponseEntity<CreateLetterResponse> viewLetter(@PathVariable Long letterCode) {
        CreateLetterResponse response = letterService.getLetter(letterCode);
        return ResponseEntity.ok(response);
    }
}