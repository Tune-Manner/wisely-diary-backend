package tuneandmanner.wiselydiarybackend.music.dto.reponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SunoApiResponse {
    @JsonProperty("code")
    private int code;

    @JsonProperty("data")
    private Data data;

    @JsonProperty("message")
    private String message;

    @Getter
    @NoArgsConstructor
    public static class Data {
        @JsonProperty("task_id")
        private String task_id;
    }

    public boolean isSuccess() {
        return code == 200;
    }
}
