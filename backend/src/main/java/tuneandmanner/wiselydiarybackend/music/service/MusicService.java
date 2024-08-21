package tuneandmanner.wiselydiarybackend.music.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.common.exception.ServerInternalException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;
import tuneandmanner.wiselydiarybackend.music.domain.repository.MusicRepository;
import tuneandmanner.wiselydiarybackend.music.dto.response.*;
import tuneandmanner.wiselydiarybackend.music.dto.request.CreateMusicRequest;
import tuneandmanner.wiselydiarybackend.music.dto.request.SunoApiRequest;
import tuneandmanner.wiselydiarybackend.rag.service.OpenAIService;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;
    private final DiaryRepository diaryRepository;
    private final SunoApiService sunoApiService;
    private final OpenAIService openAIService;

    @Transactional
    public MusicPlaybackResponse getOrCreateMusicPlayback(Long diaryCode) {

        // 1. 먼저 해당 diaryCode에 대한 음악이 이미 존재하는지 확인
        Optional<Music> existingMusic = musicRepository.findByDiaryCode(diaryCode);

        Music music;
        if (existingMusic.isPresent()) {
            music = existingMusic.get();
            log.info("Existing music found for diaryCode: {}", diaryCode);
        } else {
            log.info("No existing music found for diaryCode: {}. Creating new music.", diaryCode);
            CreateMusicResponse createResponse = createSongPrompt(new CreateMusicRequest(diaryCode));
            music = musicRepository.findByClipId(createResponse.getId())
                    .orElseThrow(() -> new NotFoundException(NOT_FOUND_CLIP_ID));
        }

        return getMusicPlayback(music.getMusicCode());
    }

    public CreateMusicResponse createSongPrompt(CreateMusicRequest request) {
        Diary diary = diaryRepository.findByDiaryCode(request.getDiaryCode())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_DIARY_CODE));

        // 1. 가사 생성
        String diaryContents = diary.getDiaryContents();
        LyricsGenerationResult result = generateLyricsAndMetadata(diaryContents);

        // 2. Suno API 호출
        SunoApiRequest sunoRequest = new SunoApiRequest(result.getLyrics(), result.getTags(), result.getTitle());
        CreateMusicResponse response = sunoApiService.createSong(sunoRequest);

        // 3. Music 엔티티 저장
        LocalDateTime createdAt = LocalDateTime.now();
        saveMusicEntity(result.getTitle(), result.getLyrics(), createdAt, response.getId(), request.getDiaryCode());

        // 4. response 반환
        return response;
    }

    private LyricsGenerationResult generateLyricsAndMetadata(String diaryContents) {
        String openAIPrompt = String.format(
                "당신은 작사가입니다. 당신은 감성적이고 위로가 되는 가사를 만들 수 있습니다." +
                "일기 요약을 참고하여 가사를 생성해주세요." +
                "일기 요약에 포함된 고유 명사들과 특정 인물의 명칭을 직접 사용하지 말고, 그 의미를 함축하여 표현해 주세요." +
                "서정적인 단어들을 위주로 선택해주세요. 단, 일기 요약에 날씨에 대한 표현이 직접적으로 없다면 날씨를 표현하지 않습니다." +
                "[Verse], [Chorus], [PreChorus] 등 송폼을 지정해주세요." +
                "일기 요약:\n'%s'\n" +
                "작사 규칙 1. 일기 요약을 바탕으로 가사를 만듭니다." +
                "작사 규칙 2. 가사를 함축하는 제목을 만듭니다." +
                "작사 규칙 3. 일기 요약을 바탕으로 어울리는 장르들을 여러 개 추출합니다." +
                "다음 형식으로 응답해주세요:\n" +
                "제목: [노래 제목]\n" +
                "장르: [콤마로 구분된 태그들]\n" +
                "가사:\n[노래 가사]",
                diaryContents
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
    protected void saveMusicEntity(String title, String lyrics, LocalDateTime createdAt, String clipId, Long diaryCode) {
        Music music = Music.create(null, title, lyrics, createdAt, clipId, diaryCode);
        musicRepository.save(music);
    }

    @Transactional
    public MusicPlaybackResponse getMusicPlayback(Long musicCode) {
        Music music = musicRepository.findByMusicCode(musicCode)
                .orElseThrow(() -> {
                    log.error("Music not found for musicCode: {}", musicCode);
                    return new NotFoundException(NOT_FOUND_CLIP_ID);
                });

        String clipId = music.getClipId();

        if (clipId == null || clipId.isEmpty()) {
            log.error("ClipId not found for musicCode: {}", musicCode);
            throw new NotFoundException(NOT_FOUND_CLIP_ID);
        }

        try {
            SunoClipResponse clipResponse = sunoApiService.getClipResponse(clipId);
            SunoClipResponse.Clip firstClip = clipResponse.getClips().get(0);

            log.info("First Clip: {}", firstClip);
            log.info("Video URL: {}", firstClip.getVideoUrl());
            log.info("Current Music Path: {}", music.getMusicPath());

            if (firstClip.getVideoUrl() != null && !firstClip.getVideoUrl().isEmpty()
                    && (music.getMusicPath() == null || music.getMusicPath().isEmpty())) {
                log.info("Updating music path...");
                music.updateMusicPath(firstClip.getVideoUrl());
                Music savedMusic = musicRepository.save(music);
                log.info("Updated Music: {}", savedMusic);
            } else {
                log.info("Not updating music path. Condition not met.");
            }

            return MusicPlaybackResponse.of(
                    music.getMusicCode(),
                    music.getMusicTitle(),
                    music.getMusicLyrics(),
                    clipResponse,
                    music.getCreatedAt(),
                    music.getDiaryCode()
            );

        } catch (Exception e) {
            log.error("Error occurred while getting music playback for musicCode: {}", musicCode, e);
            throw new ServerInternalException(ExceptionCode.FAILED_TO_GET_MUSIC_PLAYBACK);
        }
    }

    public List<InquiryMusicResponse> getMusicInfo(LocalDate date, String memberId) {
        log.info("MusicService.getMusicInfo for date: {} and memberId: {}", date, memberId);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Diary> diaries = diaryRepository.findByMemberIdAndCreatedAtBetweenAndDiaryStatus(
                memberId, startOfDay, endOfDay, "EXIST");

        log.info("Found diaries: {}", diaries);
        if (diaries.isEmpty()) {
            log.info("No diaries found for the given date and member");
            return Collections.emptyList();
        }

        List<Long> diaryCodes = diaries.stream()
                .map(Diary::getDiaryCode)
                .collect(Collectors.toList());

        List<Music> musics = musicRepository.findByDiaryCodeInAndCreatedAtBetween(
                diaryCodes, startOfDay, endOfDay);

        return musics.stream()
                .map(music -> new InquiryMusicResponse(
                        music.getMusicCode(),
                        music.getMusicTitle(),
                        music.getMusicLyrics(),
                        music.getMusicPath(),
                        music.getCreatedAt(),
                        music.getDiaryCode()))
                .collect(Collectors.toList());
    }
}
