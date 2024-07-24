package tuneandmanner.wiselydiarybackend.music.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SunoApiRequest {

    // 프롬프트
    @NotNull
    private final String lyrics;

    // 장르
    private final String tags;

    // 커스텀 모드
    private final Boolean customMode;

    // 제목
    private final String title;

}
