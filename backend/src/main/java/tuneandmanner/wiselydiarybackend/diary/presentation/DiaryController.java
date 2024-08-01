package tuneandmanner.wiselydiarybackend.diary.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.diary.service.DiaryService;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @GetMapping("/{diaryCode}/letter")
    public String generateLetter(@PathVariable Long diaryCode) {
        return diaryService.generateLetter(diaryCode);
    }
}