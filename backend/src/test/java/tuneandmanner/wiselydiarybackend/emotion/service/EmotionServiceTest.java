package tuneandmanner.wiselydiarybackend.emotion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;
import tuneandmanner.wiselydiarybackend.emotion.domain.repository.EmotionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmotionServiceTest {

    private static final int EXISTING_EMOTION_CODE = 1;
    private static final int NON_EXISTING_EMOTION_CODE = 999;
    private static final String EXISTING_EMOTION_TYPE = "emotion_worry";

    private static final List<Emotion> EXPECTED_EMOTIONS = Arrays.asList(
            new Emotion(1, "emotion_worry"),
            new Emotion(2, "emotion_proud"),
            new Emotion(3, "emotion_gratitude"),
            new Emotion(4, "emotion_unfair"),
            new Emotion(5, "emotion_anger"),
            new Emotion(6, "emotion_sadness"),
            new Emotion(7, "emotion_excitement"),
            new Emotion(8, "emotion_happiness"),
            new Emotion(9, "emotion_relaxed"),
            new Emotion(10, "emotion_confused")
    );

    @Mock
    private EmotionRepository emotionRepository;

    @InjectMocks
    private EmotionService emotionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("감정 코드로 감정 조회 - 존재하는 감정")
    void selectEmotion_ExistingEmotion() {
        // Given
        Emotion expectedEmotion = new Emotion(EXISTING_EMOTION_CODE, EXISTING_EMOTION_TYPE);
        when(emotionRepository.findById(EXISTING_EMOTION_CODE)).thenReturn(Optional.of(expectedEmotion));

        // When
        Emotion result = emotionService.selectEmotion(EXISTING_EMOTION_CODE);

        // Then
        assertNotNull(result);
        assertEquals(expectedEmotion.getEmotionCode(), result.getEmotionCode());
        assertEquals(expectedEmotion.getEmotionType(), result.getEmotionType());
        verify(emotionRepository, times(1)).findById(EXISTING_EMOTION_CODE);
    }

    @Test
    @DisplayName("감정 코드로 감정 조회 - 존재하지 않는 감정")
    void selectEmotion_NonExistingEmotion() {
        // Given
        when(emotionRepository.findById(NON_EXISTING_EMOTION_CODE)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> emotionService.selectEmotion(NON_EXISTING_EMOTION_CODE));
        verify(emotionRepository, times(1)).findById(NON_EXISTING_EMOTION_CODE);
    }

    @Test
    @DisplayName("모든 감정 조회")
    void selectAllEmotions() {
        // Given
        when(emotionRepository.findAll()).thenReturn(EXPECTED_EMOTIONS);

        // When
        List<Emotion> result = emotionService.selectAllEmotions();

        // Then
        assertNotNull(result);
        assertEquals(EXPECTED_EMOTIONS.size(), result.size());
        assertEquals(EXPECTED_EMOTIONS, result);
        verify(emotionRepository, times(1)).findAll();
    }
}