package tuneandmanner.wiselydiarybackend.letter.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.letter.dto.response.CreateLetterResponse;
import tuneandmanner.wiselydiarybackend.letter.dto.response.InquiryLetterResponse;
import tuneandmanner.wiselydiarybackend.letter.service.LetterService;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/inquiry")
    public ResponseEntity<List<InquiryLetterResponse>> inquiryLetter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String memberId) {
        log.info("Received request for letter inquiry: date={}, memberId={}", date, memberId);
        List<InquiryLetterResponse> responses = letterService.inquiryLetter(date, memberId);
        if (responses.isEmpty()) {
            log.info("No letters found for the given date and member");
            return ResponseEntity.noContent().build();
        }
        log.info("Returning {} letter(s)", responses.size());
        return ResponseEntity.ok(responses);
    }
}