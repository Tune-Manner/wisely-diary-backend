package tuneandmanner.wiselydiarybackend.letter.dto.request;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLetterRequest {

    private String message;
    private Long diarySummaryCode;

}
