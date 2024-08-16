package tuneandmanner.wiselydiarybackend.emotion.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;

public interface EmotionRepository extends JpaRepository<Emotion, Integer> {
}
