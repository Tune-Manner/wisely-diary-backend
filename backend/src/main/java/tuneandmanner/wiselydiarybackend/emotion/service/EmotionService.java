package tuneandmanner.wiselydiarybackend.emotion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;
import tuneandmanner.wiselydiarybackend.emotion.dto.request.EmotionRequest;
import tuneandmanner.wiselydiarybackend.emotion.domain.repository.EmotionRepository;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.emotion.dto.response.EmotionResponse;
import tuneandmanner.wiselydiarybackend.member.domain.entity.Member;
import tuneandmanner.wiselydiarybackend.member.domain.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final MemberRepository memberRepository;

    public EmotionResponse selectMonthlyEmotion(EmotionRequest request){
        log.info("EmotionService.selectMonthlyEmotion");

        // 1) memberId로 memberName 조회 (예시)
        String memberId = request.getMemberId();
        String memberName =     getMemberNameById(memberId);

        // 2) 월별 감정 코드 조회
        Map<Integer, Integer> yearlyEmotionCodes = getYearlyEmotionCodes(request);

        // 3) 이번달 감정 조회
        Map<Integer, Double> thisMonthEmotions = getThisMonthEmotionPercentages(request);

        // 결과를 반환
        return new EmotionResponse(memberName, yearlyEmotionCodes, thisMonthEmotions);
    }

    // 1) member_name 조회
    private String getMemberNameById(String memberId){

        Member member = memberRepository.findByMemberId(memberId);

        if (member != null) {
            return member.getMemberName();
        } else {
            throw new IllegalArgumentException("Member not found with id: " + memberId);
        }
    }

    // 2) 월별 감정 조회
    private Map<Integer, Integer> getYearlyEmotionCodes(EmotionRequest request) {
        String memberId = request.getMemberId();

        // Date가 String으로 들어온다면 LocalDate로 변환
        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);
        LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = date.withMonth(date.getMonthValue()).withDayOfMonth(date.getDayOfMonth()).atTime(23, 59, 59);

        // 다이어리 데이터를 가져옴
        List<Diary> diaries = diaryRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfYear, endOfYear);

        // 월별로 그룹화하고, 가장 많이 등장한 감정 코드 찾기, 동률일 경우 최근 일기를 우선시
        Map<Integer, Integer> mostCommonEmotionsByMonth = diaries.stream()
                .collect(Collectors.groupingBy(
                        diary -> diary.getCreatedAt().getMonthValue(),
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(Diary::getEmotionCode, Collectors.counting()),
                                emotionCounts -> emotionCounts.entrySet().stream()
                                        .max((e1, e2) -> {
                                            // 감정 코드 빈도 비교
                                            int compare = e1.getValue().compareTo(e2.getValue());
                                            if (compare == 0) {
                                                // 빈도가 동일한 경우, 가장 최근 일기의 감정 코드 선택
                                                LocalDateTime latestDiary1 = diaries.stream()
                                                        .filter(diary -> diary.getEmotionCode().equals(e1.getKey()))
                                                        .max(Comparator.comparing(Diary::getCreatedAt))
                                                        .map(Diary::getCreatedAt)
                                                        .orElse(null);

                                                LocalDateTime latestDiary2 = diaries.stream()
                                                        .filter(diary -> diary.getEmotionCode().equals(e2.getKey()))
                                                        .max(Comparator.comparing(Diary::getCreatedAt))
                                                        .map(Diary::getCreatedAt)
                                                        .orElse(null);

                                                return latestDiary1.compareTo(latestDiary2); // 가장 최근 일기를 비교
                                            }
                                            return compare;
                                        })
                                        .map(Map.Entry::getKey)
                                        .orElse(null)
                        )
                ));

        return mostCommonEmotionsByMonth;
    }



    // 3) 이번달 감정 조회
    private Map<Integer, Double> getThisMonthEmotionPercentages(EmotionRequest request) {
        String memberId = request.getMemberId();

        // Date가 String으로 들어온다면 LocalDate로 변환
        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);

        // 이번 달의 시작과 끝 날짜를 구함
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);

        log.info("Fetching emotions for member: {} for month: {}", memberId, date.getMonth());

        // 해당 월의 다이어리 데이터를 가져옴
        List<Diary> diaries = diaryRepository.findAllByMemberIdAndCreatedAtBetween(memberId, startOfMonth, endOfMonth);

        // 전체 일기의 수
        int totalEntries = diaries.size();

        // 감정 코드별로 그룹화하여 빈도 계산
        Map<Integer, Long> emotionCounts = diaries.stream()
                .collect(Collectors.groupingBy(Diary::getEmotionCode, Collectors.counting()));

        // 각 감정 코드의 퍼센티지를 계산하고 반환
        return emotionCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> totalEntries > 0 ? ((double) entry.getValue() / totalEntries) * 100 : 0.0
                )).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() == 0.0 ? null : entry.getValue() // 0%를 null로 처리
                ));
    }

}
