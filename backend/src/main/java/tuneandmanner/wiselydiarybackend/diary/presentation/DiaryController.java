package tuneandmanner.wiselydiarybackend.diary.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.diary.dto.request.DiaryDetailRequest;
import tuneandmanner.wiselydiarybackend.diary.dto.response.DiaryDetailResponse;
import tuneandmanner.wiselydiarybackend.diary.service.DiaryService;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/diary")
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping("selectdetail")
    public DiaryDetailResponse selectDetailDiary(@RequestBody DiaryDetailRequest request){
        log.info("DiaryController.selectDetailDiary");

        return diaryService.getDiaryContents(request);
    }
}
