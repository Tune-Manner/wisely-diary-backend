package tuneandmanner.wiselydiarybackend.music.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.music.dto.reponse.CreateMusicResponse;
import tuneandmanner.wiselydiarybackend.music.dto.reponse.MusicPlaybackResponse;
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.service.MusicService;

@Slf4j
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("/create")
    public ResponseEntity<CreateMusicResponse> createSong(
            @RequestBody CreateMusicRequest request
    ) {
        CreateMusicResponse response = musicService.createSongPrompt(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{musicCode}/play")
    public ResponseEntity<MusicPlaybackResponse> getMusicPlayback(@PathVariable Long musicCode) {
        MusicPlaybackResponse response = musicService.getMusicPlayback(musicCode);
        return ResponseEntity.ok(response);
    }
}
