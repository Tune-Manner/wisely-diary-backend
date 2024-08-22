package tuneandmanner.wiselydiarybackend.letter.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long> {
    Optional<Letter> findByLetterCode(Long letterCode);
    Optional<Letter> findByDiaryCode(Long diaryCode);

    List<Letter> findByDiaryCodeInAndCreatedAtBetween(List<Long> diaryCodes, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
