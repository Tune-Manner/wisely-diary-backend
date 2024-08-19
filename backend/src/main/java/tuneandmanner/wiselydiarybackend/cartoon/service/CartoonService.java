package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;
import tuneandmanner.wiselydiarybackend.cartoon.domain.repository.CartoonRepository;

import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.CreateLetterCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.request.SaveCartoonRequest;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;


import tuneandmanner.wiselydiarybackend.common.config.SupabaseStorageService;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import tuneandmanner.wiselydiarybackend.diary.domain.repository.DiaryRepository;
import tuneandmanner.wiselydiarybackend.diarysummary.domain.DiarySummary;
import tuneandmanner.wiselydiarybackend.diarysummary.repository.DiarySummaryRepository;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;
import tuneandmanner.wiselydiarybackend.emotion.domain.repository.EmotionRepository;
import tuneandmanner.wiselydiarybackend.member.domain.entity.Member;
import tuneandmanner.wiselydiarybackend.member.domain.repository.MemberRepository;
import tuneandmanner.wiselydiarybackend.rag.service.VectorStoreService;


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
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final EmotionRepository emotionRepository;
    private final VectorStoreService vectorStoreService;
    private final SupabaseStorageService supabaseStorageService;

    @Value("${image.storage.path}")  // YML에서 설정한 경로를 주입
    private String imagePath;

    private String uploadImageToSupabase(String localImagePath) {
        return supabaseStorageService.uploadImage(localImagePath);
    }


    private String downloadImage(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            String fileName = Paths.get(new URL(imageUrl).getPath()).getFileName().toString();
            Path targetPath = Paths.get(imagePath).resolve(fileName);
            Files.createDirectories(targetPath.getParent());
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Failed to download image", e);
            throw new RuntimeException("Failed to download image", e);
        }
    }
    @Transactional
    public String createCartoonPrompt(CreateCartoonRequest request) {
        log.info("CartoonService.Create cartoon prompt");

        Diary diary = diaryRepository.findById(request.getDiaryCode())
                .orElseThrow(() -> new RuntimeException("Diary summary not found"));
        Member member = memberRepository.findByMemberId(request.getMemberId());
        Emotion emotion = emotionRepository.findById(diary.getEmotionCode())
                .orElseThrow(() -> new RuntimeException("Emotion not found"));
        try {
            String cartoonPath = dalleApiService.generateCartoonPrompt(emotion, member, diary.getDiaryContents());
            // Download and upload to Supabase
            String localImagePath = downloadImage(cartoonPath);
            String supabaseUrl = uploadImageToSupabase(localImagePath);


            // 여기에 데이터베이스 저장 로직 추가
            Cartoon cartoon = Cartoon.builder()
                    .cartoonPath(supabaseUrl)
                    .diaryCode(request.getDiaryCode())
                    .createdAt(LocalDateTime.now())
                    .type("Cartoon")
                    .build();
            cartoonRepository.save(cartoon);

            return supabaseUrl;
        } catch (Exception e) {
            log.error("Error creating cartoon", e);
            throw new RuntimeException("Failed to create cartoon", e);
        }
    }

    @Transactional
    public Long saveCartoon(SaveCartoonRequest request) {
        // Download the image from the URL and upload to Supabase
        String localImagePath = downloadImage(request.getCartoonPath());
        String supabaseUrl = uploadImageToSupabase(localImagePath);

        // Save the cartoon with the Supabase URL
        Cartoon cartoon = Cartoon.builder()
                .cartoonPath(supabaseUrl)
                .diaryCode(request.getDiaryCode())
                .createdAt(LocalDateTime.now())
                .build();

        Cartoon savedCartoon = cartoonRepository.save(cartoon);
        return savedCartoon.getCartoonCode();
    }

    public List<InquiryCartoonResponse> findCartoon(LocalDate date, String memberId) {
        log.info("CartoonService.findCartoon for date: {} and memberId: {}", date, memberId);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Diary> diaries = diaryRepository.findByMemberIdAndCreatedAtBetweenAndDiaryStatus(
                memberId, startOfDay, endOfDay, "EXIST");

        log.info("로그1"+diaries);
        if (diaries.isEmpty()) {
            log.info("No diaries found for the given date and member");
            return Collections.emptyList();
        }
        log.info("로그1"+diaries);

        List<Long> diaryCodes = diaries.stream()
                .map(Diary::getDiaryCode)
                .collect(Collectors.toList());



        List<Cartoon> cartoons = cartoonRepository.findByDiaryCodeInAndCreatedAtBetween(
                diaryCodes, startOfDay, endOfDay);

        return cartoons.stream()
                .map(cartoon -> new InquiryCartoonResponse(
                        cartoon.getCartoonCode(),
                        cartoon.getCartoonPath(),
                        cartoon.getCreatedAt(),
                        cartoon.getDiaryCode(),
                        cartoon.getType()))
                .collect(Collectors.toList());
    }

    @Transactional
    public String createLetterCartoonPrompt(CreateCartoonRequest request) {
        log.info("CartoonService.createLetterCartoonPrompt 시작. diaryCode: {}", request.getDiaryCode());

        // 1. 일기와 감정 찾기
        Diary diary = diaryRepository.findById(request.getDiaryCode())
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
        Emotion emotion = emotionRepository.findById(diary.getEmotionCode())
                .orElseThrow(() -> new RuntimeException("감정을 찾을 수 없습니다."));

        String emotionContents = getEmotionContents(emotion.getEmotionCode());

        // 2. 벡터 스토어에서 유사한 문서 검색
        List<String> similarDocuments = vectorStoreService.searchSimilarCartoonDocuments(emotionContents, "image");
        if (similarDocuments.size() < 2) {
            throw new RuntimeException("감정에 대한 색감 또는 그림체 정보를 찾을 수 없습니다: " + emotionContents);
        }

        // 3. 색감과 그림체 정보 파싱
        String colors = similarDocuments.get(0).split(":", 2)[1].trim();
        String style = similarDocuments.get(1).split(":", 2)[1].trim();

        // 4. 프롬프트 구성
        String prompt = String.format(
                "감정: %s\n색감: %s\n그림체: %s\n\n'%s'\n\n을 이용해서 일기에서 주인공이 느꼈던 감정이 잘 느껴지게 매우 추상적으로 표현해서 한 장의 추상화로 그려주세요.이미지를 별도의 패널이나 섹션으로 분할하지 마십시오.",
                emotionContents, colors, style, diary.getDiaryContents()
        );

        log.info("생성된 프롬프트: {}", prompt);

        // 5. DALL-E API를 사용하여 이미지 생성
        String imageUrl = dalleApiService.generateImage(prompt);

        // Download and upload to Supabase
        String localImagePath = downloadImage(imageUrl);
        String supabaseUrl = uploadImageToSupabase(localImagePath);

        Cartoon cartoon = Cartoon.builder()
                .cartoonPath(supabaseUrl)
                .diaryCode(diary.getDiaryCode())
                .createdAt(LocalDateTime.now())
                .type("Letter")
                .build();
        cartoonRepository.save(cartoon);
        return supabaseUrl;
    }

    private String getEmotionContents(Integer emotionCode) {
        switch (emotionCode.intValue()) {
            case 1: return "걱정";
            case 2: return "뿌듯";
            case 3: return "감사";
            case 4: return "억울";
            case 5: return "분노";
            case 6: return "슬픔";
            case 7: return "설렘";
            case 8: return "신남";
            case 9: return "편안";
            case 10: return "당황";
            default: throw new RuntimeException("알 수 없는 감정 코드: " + emotionCode);
        }
    }
}
