package tuneandmanner.wiselydiarybackend.emotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@AllArgsConstructor // 모든 필드를 포함하는 생성자를 자동으로 생성합니다.
public class EmotionResponse {

    private String memberName;
    private Map<Integer, Integer> yearlyEmotions;
    private Map<Integer, Double> thisMonthEmotions;

}
