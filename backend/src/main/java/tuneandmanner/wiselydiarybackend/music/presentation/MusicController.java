package tuneandmanner.wiselydiarybackend.music.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.service.MusicService;

@Slf4j
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("/create")
    public ResponseEntity<String> createSong(
            @RequestBody CreateMusicRequest request
    ) {
        musicService.createSongPrompt(request);
        return ResponseEntity.ok().build();
    }
}
