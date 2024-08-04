package tuneandmanner.wiselydiarybackend.letter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.DiarySummary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;
import tuneandmanner.wiselydiarybackend.letter.domain.repository.LetterRepository;
import tuneandmanner.wiselydiarybackend.letter.dto.response.CreateLetterResponse;
import tuneandmanner.wiselydiarybackend.rag.service.RAGService;
import tuneandmanner.wiselydiarybackend.rag.service.VectorStoreService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode.NOT_FOUND_LETTER_CODE;
import static tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode.NOT_FOUND_SUMMARY_CODE;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final DiarySummaryRepository diarySummaryRepository;
    private final RAGService ragService;
    private final VectorStoreService vectorStoreService;

    // 편지 생성 후 저장
    @Transactional
    public CreateLetterResponse createLetter(Long diarySummaryCode) {
        DiarySummary diarySummary = diarySummaryRepository.findByDiarySummaryCode(diarySummaryCode)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_SUMMARY_CODE));

        String letterContents = generateLetterContents(diarySummary.getDiarySummaryContents());

        Letter letter = Letter.of(
                null,
                letterContents,
                LocalDateTime.now(),
                diarySummaryCode
        );

        Letter savedLetter = letterRepository.save(letter);

        return CreateLetterResponse.from(savedLetter);
    }

    private String generateLetterContents(String diarySummaryContents) {
        log.info("Generating letter for diary summary: {}", diarySummaryContents);

        String query = """
                당신은 일기를 작성한 사람의 다정한 심리상담 친구입니다.
                일기 내용을 참고해서 친구처럼 편지를 한국어로 작성해주세요.
                만약 일기 내용에 감정적인 내용을 찾을 수 없다면, 일상을 바탕으로 공감을 해주세요.
                일기 내용을 바탕으로 위로와 격려의 편지를 한국어로 작성해주세요.
                반말을 사용하면서도, 성숙하고 따듯한 말을 전해주세요.
                """;

        Map<String, String> result = ragService.generateResponse(query, diarySummaryContents, "letter");
        String letterContent = result.get("response");

        List<String> relevantQuotes = getRelevantQuotes(diarySummaryContents);
        if (!relevantQuotes.isEmpty()) {
            letterContent += "\n\n오늘과 어울리는 명언\n";
            for (String quote : relevantQuotes) {
                letterContent += "- " + quote + "\n";
            }
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
}
