package tuneandmanner.wiselydiarybackend.emotion.dto.response;

import java.util.Map;

public class EmotionResponse {

    private Map<Integer, Integer> yearlyEmotions;
    private Map<Integer, Double> thisMonthEmotions;

    // 생성자, getter, setter 추가
    public EmotionResponse(Map<Integer, Integer> yearlyEmotions, Map<Integer, Double> thisMonthEmotions) {
        this.yearlyEmotions = yearlyEmotions;
        this.thisMonthEmotions = thisMonthEmotions;
    }

    public Map<Integer, Integer> getYearlyEmotions() {
        return yearlyEmotions;
    }

    public void setYearlyEmotions(Map<Integer, Integer> yearlyEmotions) {
        this.yearlyEmotions = yearlyEmotions;
    }

    public Map<Integer, Double> getThisMonthEmotions() {
        return thisMonthEmotions;
    }

    public void setThisMonthEmotions(Map<Integer, Double> thisMonthEmotions) {
        this.thisMonthEmotions = thisMonthEmotions;
    }
}
