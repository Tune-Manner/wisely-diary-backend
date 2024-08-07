package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.DiarySummary;
import tuneandmanner.wiselydiarybackend.cartoon.domain.repository.CartoonRepository;
import tuneandmanner.wiselydiarybackend.cartoon.domain.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.SaveCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartoonService {

    private final CartoonRepository cartoonRepository;
    private final DiarySummaryRepository diarySummaryRepository;
    private final DalleApiService dalleApiService;
    private final ChatGptService chatGptService;

    @Value("${image.storage.path}")  // YML에서 설정한 경로를 주입
    private String imagePath;




    private String downloadImage(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            String fileName = UUID.randomUUID().toString() + ".png";  // 고유 파일명 생성
            Path targetPath = Paths.get(imagePath).resolve(fileName);

            Files.createDirectories(targetPath.getParent());  // 디렉토리 생성
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);  // 이미지 다운로드 및 저장

            return targetPath.toString();  // 로컬 이미지 경로 반환
        } catch (IOException e) {
            log.error("Failed to download image", e);
            throw new RuntimeException("Failed to download image", e);
        }

    }
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

            return cartoonPath;

        } catch (Exception e) {
            log.error("Error creating cartoon", e);
            throw new RuntimeException("Failed to create cartoon", e);
        }
    }

    @Transactional
    public Integer saveCartoon(SaveCartoonRequest request) {
        log.info("CartoonService.Save cartoon");

        // 이미지 URL을 로컬에 저장
        String localImagePath = downloadImage(request.getCartoonPath());

        Cartoon cartoon = Cartoon.builder()
                .cartoonPath(localImagePath)  // 로컬 경로를 저장
                .diarySummaryCode(request.getDiarySummaryCode())
                .createdAt(LocalDateTime.now())
                .build();

        Cartoon savedCartoon = cartoonRepository.save(cartoon);
        return savedCartoon.getCartoonCode();
    }

    public List<InquiryCartoonResponse> findCartoon(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Cartoon> cartoons = cartoonRepository.findByCreatedAtBetween(startOfDay,endOfDay);
        return cartoons.stream()
                .map(cartoon -> new InquiryCartoonResponse(cartoon.getCartoonCode(),cartoon.getCartoonPath(),cartoon.getCreatedAt(),cartoon.getDiarySummaryCode()))
                .collect(Collectors.toList());
    }
}
