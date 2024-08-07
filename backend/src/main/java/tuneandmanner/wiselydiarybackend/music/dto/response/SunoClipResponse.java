package tuneandmanner.wiselydiarybackend.music.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public class SunoClipResponse {
    private int code;
    private Data data;
    private String message;

    @Getter
    @ToString
    public static class Data {
        @JsonProperty("task_id")
        private String taskId;
        private String status;
        private Map<String, Clip> clips;

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
}

