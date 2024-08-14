package tuneandmanner.wiselydiarybackend.statistics.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    //월별 감정통계 조회
    public void monthlyEmotionalStatistics() {
        log.info("StatisticsController.monthlyEmotionalStatistics");
    }
}
