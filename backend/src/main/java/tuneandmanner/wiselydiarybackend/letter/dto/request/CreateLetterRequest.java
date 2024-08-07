package tuneandmanner.wiselydiarybackend.letter.dto.request;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLetterRequest {

    private String diarySummaryContents;
    private Long diarySummaryCode;
    private Long emotionCode;

}
