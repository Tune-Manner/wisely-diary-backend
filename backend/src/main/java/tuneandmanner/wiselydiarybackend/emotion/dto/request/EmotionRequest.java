package tuneandmanner.wiselydiarybackend.emotion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionRequest {
    private String memberId;
    private String date;
}
