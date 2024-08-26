package tuneandmanner.wiselydiarybackend.cartoon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.cartoon.config.CartoonConfig;
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
    private final ResourceLoader resourceLoader;
    private final CartoonConfig cartoonConfig;


    private Path getImageDirectory() throws IOException {
        Resource resource = resourceLoader.getResource(cartoonConfig.getImagePath());
        return Paths.get(resource.getURI());
    }

    @Transactional
    public String createCartoonPrompt(CreateCartoonRequest request) {
        log.info("Creating cartoon prompt for diaryCode: {}", request.getDiaryCode());

        try {
            Diary diary = diaryRepository.findById(request.getDiaryCode())
                    .orElseThrow(() -> new RuntimeException("Diary not found for diaryCode: " + request.getDiaryCode()));

            Member member = memberRepository.findByMemberId(request.getMemberId());
            if (member == null) {
                throw new RuntimeException("Member not found for memberId: " + request.getMemberId());
            }

            Emotion emotion = emotionRepository.findById(diary.getEmotionCode())
                    .orElseThrow(() -> new RuntimeException("Emotion not found for emotionCode: " + diary.getEmotionCode()));

            String cartoonPath = dalleApiService.generateCartoonPrompt(emotion, member, diary.getDiaryContents());
            log.info("Generated cartoon path: {}", cartoonPath);

            String supabaseUrl = supabaseStorageService.uploadImageFromUrl(cartoonPath);
            log.info("Uploaded image to Supabase: {}", supabaseUrl);

            Cartoon cartoon = Cartoon.builder()
                    .cartoonPath(supabaseUrl)
                    .diaryCode(request.getDiaryCode())
                    .createdAt(LocalDateTime.now())
                    .type("Cartoon")
                    .build();
            cartoonRepository.save(cartoon);

            return supabaseUrl;
        } catch (Exception e) {
            log.error("Error creating cartoon for diaryCode: {}", request.getDiaryCode(), e);
            throw new RuntimeException("Failed to create cartoon", e);
        }
    }

    @Transactional
    public Long saveCartoon(SaveCartoonRequest request) {
        log.info("Saving cartoon for diaryCode: {}", request.getDiaryCode());

        try {
            String supabaseUrl = supabaseStorageService.uploadImageFromUrl(request.getCartoonPath());

            Cartoon cartoon = Cartoon.builder()
                    .cartoonPath(supabaseUrl)
                    .diaryCode(request.getDiaryCode())
                    .createdAt(LocalDateTime.now())
                    .build();

            Cartoon savedCartoon = cartoonRepository.save(cartoon);
            log.info("Saved cartoon with code: {}", savedCartoon.getCartoonCode());
            return savedCartoon.getCartoonCode();
        } catch (Exception e) {
            log.error("Error saving cartoon for diaryCode: {}", request.getDiaryCode(), e);
            throw new RuntimeException("Failed to save cartoon", e);
        }
    }

    public List<InquiryCartoonResponse> findCartoon(LocalDate date, String memberId) {
        log.info("Finding cartoons for date: {} and memberId: {}", date, memberId);

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
        log.info("Creating letter cartoon prompt for diaryCode: {}", request.getDiaryCode());

        try {
            Diary diary = diaryRepository.findById(request.getDiaryCode())
                    .orElseThrow(() -> new RuntimeException("Diary not found for diaryCode: " + request.getDiaryCode()));

            Emotion emotion = emotionRepository.findById(diary.getEmotionCode())
                    .orElseThrow(() -> new RuntimeException("Emotion not found for emotionCode: " + diary.getEmotionCode()));

            String emotionContents = getEmotionContents(emotion.getEmotionCode());

            List<String> similarDocuments = vectorStoreService.searchSimilarCartoonDocuments(emotionContents, "image");
            if (similarDocuments.size() < 2) {
                throw new RuntimeException("Insufficient similar documents found for emotion: " + emotionContents);
            }

            String colors = similarDocuments.get(0).split(":", 2)[1].trim();
            String style = similarDocuments.get(1).split(":", 2)[1].trim();

            String prompt = String.format(
                    "감정: %s\n색감: %s\n그림체: %s\n\n'%s'\n\n을 이용해서 일기에서 주인공이 느꼈던 감정이 잘 느껴지게 매우 추상적으로 표현해서 한 장의 추상화로 그려주세요. 이미지를 별도의 패널이나 섹션으로 분할하지 마십시오.",
                    emotionContents, colors, style, diary.getDiaryContents()
            );

            log.info("Generated prompt: {}", prompt);

            String imageUrl = dalleApiService.generateImage(prompt);
            log.info("Generated image URL: {}", imageUrl);

            String supabaseUrl = supabaseStorageService.uploadImageFromUrl(imageUrl);
            log.info("Uploaded image to Supabase: {}", supabaseUrl);

            Cartoon cartoon = Cartoon.builder()
                    .cartoonPath(supabaseUrl)
                    .diaryCode(diary.getDiaryCode())
                    .createdAt(LocalDateTime.now())
                    .type("Letter")
                    .build();
            cartoonRepository.save(cartoon);

            return supabaseUrl;
        } catch (Exception e) {
            log.error("Error creating letter cartoon for diaryCode: {}", request.getDiaryCode(), e);
            throw new RuntimeException("Failed to create letter cartoon", e);
        }
    }

    private String getEmotionContents(Integer emotionCode) {
        switch (emotionCode) {
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
