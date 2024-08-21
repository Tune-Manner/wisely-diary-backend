package tuneandmanner.wiselydiarybackend.letter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;
import tuneandmanner.wiselydiarybackend.letter.domain.repository.LetterRepository;
import tuneandmanner.wiselydiarybackend.letter.dto.response.CreateLetterResponse;
import tuneandmanner.wiselydiarybackend.letter.dto.response.InquiryLetterResponse;
import tuneandmanner.wiselydiarybackend.rag.service.RAGService;
import tuneandmanner.wiselydiarybackend.rag.service.VectorStoreService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final DiaryRepository diaryRepository;
    private final RAGService ragService;
    private final VectorStoreService vectorStoreService;

    @Transactional
    public CreateLetterResponse getOrCreateLetter(Long diaryCode) {
        // 1. 먼저 기존 편지가 있는지 확인
        Optional<Letter> existingLetter = letterRepository.findByDiaryCode(diaryCode);
        if (existingLetter.isPresent()) {
            return CreateLetterResponse.from(existingLetter.get());
        }

        // 2. 기존 편지가 없다면, 새 편지 생성
        return createNewLetter(diaryCode);
    }

    private CreateLetterResponse createNewLetter(Long diaryCode) {
        // 일기의 존재 여부 확인
        Diary diary = diaryRepository.findByDiaryCode(diaryCode)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND_DIARY_CODE));

        String letterContents = generateLetterContents(diary.getDiaryContents());

        Letter newLetter = Letter.of(
                null,
                letterContents,
                LocalDateTime.now(),
                diaryCode
        );

        Letter savedLetter = letterRepository.save(newLetter);
        return CreateLetterResponse.from(savedLetter);
    }

    private String generateLetterContents(String diaryContents) {
        // 기존의 편지 내용 생성 로직
        log.info("Generating letter for diary : {}", diaryContents);

        List<String> relevantQuotes = getRelevantQuotes(diaryContents);
        StringBuilder quotesContent = new StringBuilder();
        if (!relevantQuotes.isEmpty()) {
            quotesContent.append("오늘과 어울리는 명언\n");
            for (String quote : relevantQuotes) {
                quotesContent.append("- ").append(quote).append("\n");
            }
            quotesContent.append("\n");
        }

        String query = """
                당신은 일기를 작성한 사람의 다정한 심리상담 친구입니다.
                일기 내용을 참고해서 친구처럼 편지를 한국어로 작성해주세요.
                만약 일기 내용에 감정적인 내용을 찾을 수 없다면, 일상을 바탕으로 공감을 해주세요. 이 내용은 편지에 포함하지 않아도 됩니다.
                일기 내용을 바탕으로 위로와 격려의 편지를 한국어로 작성해주세요.
                반말을 사용하면서도, 성숙하고 따듯한 말을 전해주세요.
                """ + quotesContent + """
                또한 이 명언을 참고해서 위에 제시된 명언을 자연스럽게 활용하여 현재에 대한 조언을 해주세요.
                편지 내용에 명언을 포함할 때에는 '위 명언처럼' 등과 같이 자연스럽게 작성되도록 해주세요.
                편지의 마무리는 당신을 응원하는 친구가. 라는 내용으로 마무리 해주세요.
                """;

        Map<String, String> result = ragService.generateResponse(query, diaryContents, "letter");
        String letterContent = result.get("response");

        if (!relevantQuotes.isEmpty()) {
            StringBuilder finalContent = new StringBuilder("오늘의 명언:\n");
            for (String quote : relevantQuotes) {
                finalContent.append("- ").append(quote).append("\n");
            }
            finalContent.append("\n").append(letterContent);
            letterContent = finalContent.toString();
        }

        return letterContent;
    }


    // 명언 추출
    private List<String> getRelevantQuotes(String diarySummaryContents) {
        try {
            return vectorStoreService.searchSimilarDocuments(diarySummaryContents, "letter");
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get relevant quotes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // 편지 조회
    @Transactional(readOnly = true)
    public CreateLetterResponse getLetter(Long letterCode) {
        Letter letter = letterRepository.findByLetterCode(letterCode)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_LETTER_CODE));

        return CreateLetterResponse.from(letter);
    }

    public List<InquiryLetterResponse> inquiryLetter(LocalDate date, String memberId) {
        log.info("LetterService.inquiryLetter for date: {} and memberId: {}", date, memberId);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Diary> diaries = diaryRepository.findByMemberIdAndCreatedAtBetweenAndDiaryStatus(
                memberId, startOfDay, endOfDay, "EXIST");

        if (diaries.isEmpty()) {
            log.info("No diaries found for the given date and member");
            return Collections.emptyList();
        }

        List<Long> diaryCodes = diaries.stream()
                .map(Diary::getDiaryCode)
                .collect(Collectors.toList());

        List<Letter> letters = letterRepository.findByDiaryCodeInAndCreatedAtBetween(
                diaryCodes, startOfDay, endOfDay);

        return letters.stream()
                .map(letter -> new InquiryLetterResponse(
                        letter.getLetterCode(),
                        letter.getLetterContents(),
                        letter.getCreatedAt(),
                        letter.getDiaryCode()))
                .collect(Collectors.toList());
    }
}
