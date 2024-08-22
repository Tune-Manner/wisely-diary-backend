package tuneandmanner.wiselydiarybackend.music.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryMusicResponse {

    private final Long musicCode;
    private final String musicTitle;
    private final String musicLyrics;
    private final String musicPath;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;
    private final Long diaryCode;

    public InquiryMusicResponse(Long musicCode, String musicTitle, String musicLyrics,
                                String musicPath, LocalDateTime createdAt, Long diaryCode) {
        this.musicCode = musicCode;
        this.musicTitle = musicTitle;
        this.musicLyrics = musicLyrics;
        this.musicPath = musicPath;
        this.createdAt = createdAt;
        this.diaryCode = diaryCode;
    }
}
