package tuneandmanner.wiselydiarybackend.cartoon.dto.response;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCartoonResponse {
    private Long cartoonCode;
    private String cartoonPath;
    private LocalDateTime createdAt;
    private Long diarySummaryCode;
}
