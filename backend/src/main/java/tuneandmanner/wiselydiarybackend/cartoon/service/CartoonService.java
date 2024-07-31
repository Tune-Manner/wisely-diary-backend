package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.DiarySummary;
import tuneandmanner.wiselydiarybackend.cartoon.domain.repository.CartoonRepository;
import tuneandmanner.wiselydiarybackend.cartoon.domain.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateCartoonRequest;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartoonService {

    private final CartoonRepository cartoonRepository;
    private final DiarySummaryRepository diarySummaryRepository;
    private final DalleApiService dalleApiService;
    private final ChatGptService chatGptService;

    @Transactional
    public String createCartoonPrompt(CreateCartoonRequest request) {
        log.info("CartoonService.Create cartoon prompt");

        DiarySummary diarySummary = diarySummaryRepository.findById(request.getDiarySummaryCode())
                .orElseThrow(() -> new RuntimeException("Diary summary not found"));

        try {
            // ChatGptService에서 prompt생성
            String generatedPrompt = chatGptService.generateCartoonPrompt(diarySummary.getDiarySummaryContents());

            // Use the generated prompt to create an image with DalleApiService
            String cartoonPath = dalleApiService.generateCartoonPrompt(generatedPrompt);

            Cartoon cartoon = Cartoon.builder()
                    .cartoonPath(cartoonPath)
                    .diarySummaryCode(request.getDiarySummaryCode())
                    .createdAt(LocalDateTime.now())
                    .build();

            cartoonRepository.save(cartoon);
            return cartoonPath;

        } catch (Exception e) {
            log.error("Error creating cartoon", e);
            throw new RuntimeException("Failed to create cartoon", e);
        }
    }
}
