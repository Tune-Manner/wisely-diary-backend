package tuneandmanner.wiselydiarybackend.music.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LyricsGenerationResult {
    private final String title;
    private final String lyrics;
    private final String tags;

    public static LyricsGenerationResult of(String title, String lyrics, String tags) {
        return new LyricsGenerationResult(title, lyrics, tags);
    }
}
