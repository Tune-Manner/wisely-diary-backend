package tuneandmanner.wiselydiarybackend.music.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class SunoClipResponse {
    private String id;
    private List<Clip> clips;
    private String status;

    @Getter
    @ToString
    public static class Clip {
        private String id;
        @JsonProperty("video_url")
        private String videoUrl;
        @JsonProperty("audio_url")
        private String audioUrl;
        @JsonProperty("image_url")
        private String imageUrl;
        private String status;
        private String title;
    }
}

