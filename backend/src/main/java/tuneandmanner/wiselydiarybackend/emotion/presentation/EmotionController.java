package tuneandmanner.wiselydiarybackend.emotion.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;
import tuneandmanner.wiselydiarybackend.emotion.dto.request.EmotionRequest;
import tuneandmanner.wiselydiarybackend.emotion.dto.response.EmotionResponse;
import tuneandmanner.wiselydiarybackend.emotion.service.EmotionService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistics")
public class EmotionController {

    private final EmotionService statisticsService;

    //월별 감정통계 조회
    @GetMapping("/inquire")
    public ResponseEntity<EmotionResponse> monthlyEmotionalStatistics(@RequestBody EmotionRequest request) {
        log.info("StatisticsController.monthlyEmotionalStatistics");

        EmotionResponse result = statisticsService.selectMonthlyEmotion(request);

        return ResponseEntity.ok(result);
    }
}
