package tuneandmanner.wiselydiarybackend.emotion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;
import tuneandmanner.wiselydiarybackend.emotion.dto.request.EmotionRequest;
import tuneandmanner.wiselydiarybackend.emotion.domain.repository.EmotionRepository;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;

    public Map<String, List<Emotion>> selectMonthlyEmotion(EmotionRequest request){
        log.info("EmotionService.selectMonthlyEmotion");

        // 1) 월별 감정 조회
        List<Emotion> yearlyEmotions = getYearlyEmotions(request);

        // 2) 이번달 감정 조회
        List<Emotion> thisMonthEmotions = getThisMonthEmotions(request);

        // 결과를 맵으로 반환
        return Map.of(
                "yearlyEmotions", yearlyEmotions,
                "thisMonthEmotions", thisMonthEmotions
        );
    }

    // 1) 월별 감정 조회
    private List<Emotion> getYearlyEmotions(EmotionRequest request) {
        String memberId = request.getMemberId();

        // Date가 String으로 들어온다면 LocalDate로 변환
        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);

        LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = date.withMonth(12).withDayOfMonth(31).atTime(23, 59, 59);

        log.info("Fetching yearly emotions for member: {} for year: {}", memberId, date.getYear());

        // 새로 추가한 메서드 사용
        List<Diary> diaries = diaryRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfYear, endOfYear);
        return diaries.stream()
                .map(diary -> emotionRepository.findById(diary.getEmotionCode()).orElse(null))
                .collect(Collectors.toList());
    }

    // 2) 이번달 감정 조회
    private List<Emotion> getThisMonthEmotions(EmotionRequest request) {
        String memberId = request.getMemberId();

        // Date가 String으로 들어온다면 LocalDate로 변환
        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);

        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);

        log.info("Fetching emotions for member: {} for month: {}", memberId, date.getMonth());

        // 새로 추가한 메서드 사용
        List<Diary> diaries = diaryRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfMonth, endOfMonth);
        return diaries.stream()
                .map(diary -> emotionRepository.findById(diary.getEmotionCode()).orElse(null))
                .collect(Collectors.toList());
    }
}
