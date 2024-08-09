package tuneandmanner.wiselydiarybackend.music.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;
import tuneandmanner.wiselydiarybackend.diarysummary.domain.DiarySummary;
import tuneandmanner.wiselydiarybackend.diarysummary.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;
import tuneandmanner.wiselydiarybackend.music.domain.repository.MusicRepository;
import tuneandmanner.wiselydiarybackend.music.dto.response.CreateMusicResponse;
import tuneandmanner.wiselydiarybackend.music.dto.response.LyricsGenerationResult;
import tuneandmanner.wiselydiarybackend.music.dto.response.MusicPlaybackResponse;
import tuneandmanner.wiselydiarybackend.music.dto.response.SunoClipResponse;
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.dto.request.SunoApiRequest;
import tuneandmanner.wiselydiarybackend.rag.service.OpenAIService;


import static tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode.NOT_FOUND_SUMMARY_CODE;


@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final DiarySummaryRepository diarySummaryRepository;
    private final SunoApiService sunoApiService;
    private final OpenAIService openAIService;

    public CreateMusicResponse createSongPrompt(CreateMusicRequest request) {

        DiarySummary diarySummary = diarySummaryRepository.findByDiarySummaryCode(request.getDiarySummaryCode())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_SUMMARY_CODE));

        // 1. 가사 생성
        String diarySummaryContents = diarySummary.getDiarySummaryContents();

        LyricsGenerationResult result = generateLyricsAndMetadata(diarySummaryContents);
        Boolean customMode = true;

        // 2. Suno API 호출
        SunoApiRequest sunoRequest = new SunoApiRequest(result.getLyrics(), result.getTags(), customMode, result.getTitle());
        CreateMusicResponse response = sunoApiService.createSong(sunoRequest);

        // 3. Music 엔티티 저장
        saveMusicEntity(result.getTitle(), result.getLyrics(), response.getTaskId());

        return response;
    }

    private LyricsGenerationResult generateLyricsAndMetadata(String diarySummary) {
        String openAIPrompt = String.format(
                "다음 일기 요약을 바탕으로 가사를 만들어주세요:\n'%s'\n" +
                        "다음 형식으로 응답해주세요:\n" +
                        "제목: [노래 제목]\n" +
                        "장르: [콤마로 구분된 태그들]\n" +
                        "가사:\n[노래 가사]",
                diarySummary
        );

        Prompt promptContent = new Prompt(openAIPrompt);
        String response = openAIService.generateResponse(promptContent);
        return parseLyricsGenerationResult(response);
    }

    private LyricsGenerationResult parseLyricsGenerationResult(String response) {
        String[] parts = response.split("\n", 4);
        String title = parts[0].substring("제목: ".length()).trim();
        String tags = parts[1].substring("장르: ".length()).trim();
        String lyrics = parts[3].trim();

        return LyricsGenerationResult.of(title, lyrics, tags);
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
