package tuneandmanner.wiselydiarybackend.letter.dto.request;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLetterRequest {

    private String diaryContents;
    private Long diaryCode;
    private Long emotionCode;

}
