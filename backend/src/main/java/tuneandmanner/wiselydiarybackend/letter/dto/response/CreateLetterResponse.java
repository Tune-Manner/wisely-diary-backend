package tuneandmanner.wiselydiarybackend.letter.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateLetterResponse {

    private final Long letterCode;
    private final Long diaryCode;
    private final String letterContents;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;

    public static CreateLetterResponse from(Letter letter) {
        return new CreateLetterResponse(
                letter.getLetterCode(),
                letter.getDiaryCode(),
                letter.getLetterContents(),
                letter.getCreatedAt()
        );
    }
}
