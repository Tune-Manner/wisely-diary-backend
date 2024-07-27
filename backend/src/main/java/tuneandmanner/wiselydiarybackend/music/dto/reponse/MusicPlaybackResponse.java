package tuneandmanner.wiselydiarybackend.music.dto.reponse;

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
    private final String playbackUrl;
    private final LocalDateTime createdAt;
    private final Long diarySummaryCode;

    public static MusicPlaybackResponse of(Long musicCode, String musicTitle, String musicLyrics,
                                           String playbackUrl, LocalDateTime createdAt, Long diarySummaryCode) {
        return new MusicPlaybackResponse(musicCode, musicTitle, musicLyrics, playbackUrl, createdAt, diarySummaryCode);
    }
}
