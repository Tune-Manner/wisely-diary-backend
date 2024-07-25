package tuneandmanner.wiselydiarybackend.music.dto.reponse;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateMusicResponse {

    private final String taskId;

    public static CreateMusicResponse from(final Music music) {
        return new CreateMusicResponse(
                music.getTaskId()
        );
    }
}
