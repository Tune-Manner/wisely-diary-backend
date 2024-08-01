package tuneandmanner.wiselydiarybackend.diary.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.diary.service.DiaryService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @GetMapping("/rag/letter/{diaryCode}")
    public Map<String, String> generateLetter(@PathVariable Long diaryCode) {
        return diaryService.generateLetter(diaryCode);
    }
}