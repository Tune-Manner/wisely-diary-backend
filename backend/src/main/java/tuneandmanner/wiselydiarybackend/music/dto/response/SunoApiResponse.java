package tuneandmanner.wiselydiarybackend.music.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SunoApiResponse {
    private String id;
    private List<Clip> clips;
    private Metadata metadata;
    @JsonProperty("major_model_version")
    private String majorModelVersion;
    private String status;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("batch_size")
    private int batchSize;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Clip {
        private String id;
        @JsonProperty("video_url")
        private String videoUrl;
        @JsonProperty("audio_url")
        private String audioUrl;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("image_large_url")
        private String imageLargeUrl;
        @JsonProperty("is_video_pending")
        private boolean isVideoPending;
        @JsonProperty("major_model_version")
        private String majorModelVersion;
        @JsonProperty("model_name")
        private String modelName;
        private Metadata metadata;
        @JsonProperty("is_liked")
        private boolean isLiked;
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("display_name")
        private String displayName;
        private String handle;
        @JsonProperty("is_handle_updated")
        private boolean isHandleUpdated;
        @JsonProperty("avatar_image_url")
        private String avatarImageUrl;
        @JsonProperty("is_trashed")
        private boolean isTrashed;
        private String reaction;
        @JsonProperty("created_at")
        private String createdAt;
        private String status;
        private String title;
        @JsonProperty("play_count")
        private int playCount;
        @JsonProperty("upvote_count")
        private int upvoteCount;
        @JsonProperty("is_public")
        private boolean isPublic;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String prompt;
        private String tags;
        private String type;
        @JsonProperty("has_vocal")
        private boolean hasVocal;
        @JsonProperty("gpt_description_prompt")
        private String gptDescriptionPrompt;
        @JsonProperty("audio_prompt_id")
        private String audioPromptId;
        private List<History> history;
        @JsonProperty("concat_history")
        private String concatHistory;
        private String duration;
        @JsonProperty("refund_credits")
        private String refundCredits;
        private boolean stream;
        private boolean infill;
        @JsonProperty("is_audio_upload_tos_accepted")
        private boolean isAudioUploadTosAccepted;
        @JsonProperty("error_type")
        private String errorType;
        @JsonProperty("error_message")
        private String errorMessage;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class History {
        private String id;
        @JsonProperty("continue_at")
        private double continueAt;
        private String type;
        private String source;
        private boolean infill;
    }
}