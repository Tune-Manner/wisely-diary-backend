package tuneandmanner.wiselydiarybackend.music.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateMusicRequest {

    // 프롬프트
    @NotNull
    @Size(max = 3000)
    private final String prompt;

    // 장르
    private final String tags;

}
