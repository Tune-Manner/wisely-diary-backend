package tuneandmanner.wiselydiarybackend.music.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MusicPlaybackResponse {

    private final Long musicCode;
    private final String musicTitle;
    private final String musicLyrics;
    private final SunoClipResponse clipResponse;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;
    private final Long diaryCode;

    public static MusicPlaybackResponse of(Long musicCode, String musicTitle, String musicLyrics,
                                           SunoClipResponse clipResponse, LocalDateTime createdAt, Long diaryCode) {
        return new MusicPlaybackResponse(musicCode, musicTitle, musicLyrics, clipResponse, createdAt, diaryCode);
    }
}
