package tuneandmanner.wiselydiarybackend.music.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tuneandmanner.wiselydiarybackend.music.dto.response.CreateMusicResponse;
import tuneandmanner.wiselydiarybackend.music.dto.response.InquiryMusicResponse;
import tuneandmanner.wiselydiarybackend.music.dto.response.MusicPlaybackResponse;
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.service.MusicService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @GetMapping("/check/{diaryCode}")
    public ResponseEntity<MusicPlaybackResponse> getOrCreateMusicPlayback(@PathVariable Long diaryCode) {
        MusicPlaybackResponse response = musicService.getOrCreateMusicPlayback(diaryCode);
        return ResponseEntity.ok(response);
    }

    // 삭제 예정
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

    @GetMapping("/inquiry")
    public ResponseEntity<List<InquiryMusicResponse>> getMusicInfo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String memberId
    ) {
        log.info("Received request for music info: date={}, memberId={}", date, memberId);
        List<InquiryMusicResponse> musicInfoList = musicService.getMusicInfo(date, memberId);
        if (musicInfoList.isEmpty()) {
            log.info("No music info found for the given date and member");
            return ResponseEntity.noContent().build();
        }
        log.info("Returning {} music info items", musicInfoList.size());
        return ResponseEntity.ok(musicInfoList);
    }
}
