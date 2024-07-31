package tuneandmanner.wiselydiarybackend.letter.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;

import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long> {
    Optional<Letter> findByLetterCode(Long letterCode);
}
