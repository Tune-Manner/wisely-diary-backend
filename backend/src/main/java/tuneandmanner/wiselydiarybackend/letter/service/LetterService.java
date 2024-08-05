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

        List<String> relevantQuotes = getRelevantQuotes(diarySummaryContents);
        StringBuilder quotesContent = new StringBuilder();
        if (!relevantQuotes.isEmpty()) {
            quotesContent.append("오늘과 어울리는 명언\n");
            for (String quote : relevantQuotes) {
                quotesContent.append("- ").append(quote).append("\n");
            }
            quotesContent.append("\n");
        }

        // 명언을 포함한 query 생성
        String query = """
                당신은 일기를 작성한 사람의 다정한 심리상담 친구입니다.
                일기 내용을 참고해서 친구처럼 편지를 한국어로 작성해주세요.
                만약 일기 내용에 감정적인 내용을 찾을 수 없다면, 일상을 바탕으로 공감을 해주세요. 이 내용은 편지에 포함하지 않아도 됩니다.
                일기 내용을 바탕으로 위로와 격려의 편지를 한국어로 작성해주세요.
                반말을 사용하면서도, 성숙하고 따듯한 말을 전해주세요.
                """ + quotesContent + """
                또한 이 명언을 참고해서 위에 제시된 명언을 자연스럽게 활용하여 현재에 대한 조언을 해주세요. 편지 내용에 명언을 포함할 
                때에는 '위 명언처럼' 등과 같이 자연스럽게 작성되도록 해주세요.
                """;

        Map<String, String> result = ragService.generateResponse(query, diarySummaryContents, "letter");
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
}
