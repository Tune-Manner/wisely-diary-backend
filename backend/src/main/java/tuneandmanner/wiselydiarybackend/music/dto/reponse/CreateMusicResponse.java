package tuneandmanner.wiselydiarybackend.music.dto.reponse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateMusicResponse {
    private final String taskId;

    public static CreateMusicResponse from(SunoApiResponse apiResponse) {
        if (!apiResponse.isSuccess()) {
            throw new RuntimeException("음악 생성 실패: " + apiResponse.getMessage());
        }
        return new CreateMusicResponse(apiResponse.getData().getTask_id());
    }
}
