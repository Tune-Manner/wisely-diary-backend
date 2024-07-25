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
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.dto.request.SunoApiRequest;


@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final SunoApiService sunoApiService;

    @Transactional
    public void createSongPrompt(CreateMusicRequest request) {

        // 1. 요약 받아서 가사로 변환
        String lyrics = request.getPrompt();
        String tags = request.getTags();
        Boolean customMode = true;
        String title = "test title";

        log.info(request.getPrompt());

        // 2. RAG 미정

        // 3. Suno API 호출
        SunoApiRequest sunoRequest = new SunoApiRequest(lyrics, tags, customMode, title);
        sunoApiService.createSong(sunoRequest);
    }

    @Transactional
    public CreateMusicResponse getMusic(String taskId) {

        Music music = musicRepository.findByTaskId(taskId)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND_TASK_ID));

        return CreateMusicResponse.from(music);
    }
}
