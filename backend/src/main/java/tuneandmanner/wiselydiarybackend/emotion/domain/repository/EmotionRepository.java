package tuneandmanner.wiselydiarybackend.emotion.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tuneandmanner.wiselydiarybackend.emotion.domain.entity.Emotion;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Integer> {

}
