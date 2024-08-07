package tuneandmanner.wiselydiarybackend.music.dto.response;

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
    private final LocalDateTime createdAt;
    private final Long diarySummaryCode;

    public static MusicPlaybackResponse of(Long musicCode, String musicTitle, String musicLyrics,
                                           SunoClipResponse clipResponse, LocalDateTime createdAt, Long diarySummaryCode) {
        return new MusicPlaybackResponse(musicCode, musicTitle, musicLyrics, clipResponse, createdAt, diarySummaryCode);
    }
}
