package tuneandmanner.wiselydiarybackend.letter.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class CreateLetterResponse {

    private final Long letterCode;
    private final Long diarySummaryCode;
    private final String letterContents;
    private final LocalDateTime createdAt;

    public static CreateLetterResponse from(Letter letter) {

        return new CreateLetterResponse(
                letter.getLetterCode(),
                letter.getDiarySummaryCode(),
                letter.getLetterContents(),
                letter.getCreatedAt()
        );
    }
}
