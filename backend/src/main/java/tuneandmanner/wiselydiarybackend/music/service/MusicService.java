package tuneandmanner.wiselydiarybackend.music.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;
import tuneandmanner.wiselydiarybackend.music.domain.repository.MusicRepository;
import tuneandmanner.wiselydiarybackend.music.dto.reponse.CreateMusicResponse;
import tuneandmanner.wiselydiarybackend.music.dto.reponse.MusicPlaybackResponse;
import tuneandmanner.wiselydiarybackend.music.dto.reponse.SunoClipResponse;
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.dto.request.SunoApiRequest;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final SunoApiService sunoApiService;

    public CreateMusicResponse createSongPrompt(CreateMusicRequest request) {
        // 1. 요약 받아서 가사로 변환 (RAG 사용 예정)
        String lyrics = request.getPrompt();
        String tags = request.getTags();
        Boolean customMode = true;
        String title = "Untitled";

        // 2. Suno API 호출
        SunoApiRequest sunoRequest = new SunoApiRequest(lyrics, tags, customMode, title);
        CreateMusicResponse response = sunoApiService.createSong(sunoRequest);

        // 3. Music 엔티티 저장
        saveMusicEntity(title, lyrics, response.getTaskId());

        return response;
    }

    @Transactional
    protected void saveMusicEntity(String title, String lyrics, String taskId) {
        Music music = Music.create("", title, lyrics, taskId);
        musicRepository.save(music);
    }

    public MusicPlaybackResponse getMusicPlayback(Long musicCode) {
        Music music = musicRepository.findById(musicCode)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND_TASK_ID));

        SunoClipResponse clipResponse = sunoApiService.getClipResponse(music.getTaskId());

        log.info("SunoClipResponse: {}", clipResponse);
        log.info("Data: {}", clipResponse.getData());

        if (clipResponse.getData() == null || clipResponse.getData().getClips() == null || clipResponse.getData().getClips().isEmpty()) {
            throw new NotFoundException(ExceptionCode.NOT_FOUND_CLIP_URL);
        }

        SunoClipResponse.Data.Clip firstClip = clipResponse.getData().getClips().values().iterator().next();
        log.info("First Clip: {}", firstClip);

        if (firstClip.getVideoUrl() == null && firstClip.getAudioUrl() == null) {
            throw new NotFoundException(ExceptionCode.NOT_FOUND_CLIP_URL);
        }

        return MusicPlaybackResponse.of(
                music.getMusicCode(),
                music.getMusicTitle(),
                music.getMusicLyrics(),
                clipResponse,
                music.getCreatedAt(),
                music.getDiarySummaryCode()
        );
    }

}
