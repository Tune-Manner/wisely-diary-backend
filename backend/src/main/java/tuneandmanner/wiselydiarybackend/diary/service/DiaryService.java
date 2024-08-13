package tuneandmanner.wiselydiarybackend.diary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.diary.dto.request.DiaryDetailRequest;
import tuneandmanner.wiselydiarybackend.diary.dto.response.DiaryDetailResponse;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Service
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public DiaryDetailResponse getDiaryContents(DiaryDetailRequest request) {
        log.info("DiaryService.getDiaryContents - memberId: {}, date: {}", request.getMemberId(), request.getDate());

        LocalDate date = LocalDate.parse(request.getDate(), DateTimeFormatter.ISO_DATE);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        String diaryContents = diaryRepository.findByMemberIdAndCreatedAtBetween(request.getMemberId(), startOfDay, endOfDay)
                .map(Diary::getDiaryContents)
                .orElse("해당 날짜의 일기를 찾을 수 없습니다.");

        return new DiaryDetailResponse(diaryContents);
    }
}
