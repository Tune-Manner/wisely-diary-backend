package tuneandmanner.wiselydiarybackend.cartoon.dto.response;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCartoonResponse {
    private Integer cartoonCode;
    private String cartoonPath;
    private LocalDateTime createdAt;
    private Integer diarySummaryCode;
}
