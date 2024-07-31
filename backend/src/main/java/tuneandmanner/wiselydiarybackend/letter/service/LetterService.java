package tuneandmanner.wiselydiarybackend.letter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.common.rag.letter.LetterAssistantCommand;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;
import tuneandmanner.wiselydiarybackend.letter.domain.repository.LetterRepository;
import tuneandmanner.wiselydiarybackend.letter.dto.request.CreateLetterRequest;
import tuneandmanner.wiselydiarybackend.letter.dto.response.CreateLetterResponse;

import java.time.LocalDateTime;

import static tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode.NOT_FOUND_LETTER_CODE;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final LetterAssistantCommand letterAssistantCommand;

    // 편지 생성 후 저장
    public CreateLetterResponse createLetter(CreateLetterRequest request) {
        String letterContents = letterAssistantCommand.generateLetterContents(request.getMessage());

        Letter newLetter = Letter.of(
                null, // letterCode는 null로 전달하고 저장 시 자동 생성
                request.getDiarySummaryCode(),
                letterContents,
                LocalDateTime.now()
        );


        Letter savedLetter = letterRepository.save(newLetter);
        log.info("생성된 편지 코드: {}", savedLetter.getLetterCode());

        return CreateLetterResponse.from(savedLetter);
    }

    // 편지 조회
    @Transactional(readOnly = true)
    public CreateLetterResponse getLetter(Long letterCode) {

        Letter letter = letterRepository.findByLetterCode(letterCode)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND_LETTER_CODE));

        return CreateLetterResponse.from(letter);
    }
}
