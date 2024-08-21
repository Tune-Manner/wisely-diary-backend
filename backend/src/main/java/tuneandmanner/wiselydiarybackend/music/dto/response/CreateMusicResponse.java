package tuneandmanner.wiselydiarybackend.music.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateMusicResponse {

    private final String id;
    private final List<SunoApiResponse.Clip> clips;

    public static CreateMusicResponse from(SunoApiResponse apiResponse) {
        if (!apiResponse.getStatus().equals("complete")) {
            throw new RuntimeException("음악 생성 실패: " + apiResponse.getStatus());
        }

        List<SunoApiResponse.Clip> clips = apiResponse.getClips();  // clips 가져오기
        if (clips.size() < 2) {
            throw new RuntimeException("생성된 음악 클립이 2개 미만입니다.");
        }

        String secondId = clips.get(1).getId();
        return new CreateMusicResponse(secondId, clips);  // clips 추가
    }
}
