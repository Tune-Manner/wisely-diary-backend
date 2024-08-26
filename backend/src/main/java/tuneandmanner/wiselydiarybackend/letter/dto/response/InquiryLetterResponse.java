package tuneandmanner.wiselydiarybackend.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryLetterResponse {

    private final Long letterCode;
    private final String letterContents;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;
    private final Long diaryCode;

    public InquiryLetterResponse(Long letterCode, String letterContents, LocalDateTime createdAt, Long diaryCode) {
        this.letterCode = letterCode;
        this.letterContents = letterContents;
        this.createdAt = createdAt;
        this.diaryCode = diaryCode;
    }
}
