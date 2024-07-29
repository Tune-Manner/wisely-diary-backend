package tuneandmanner.wiselydiarybackend.letter.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.letter.domain.entity.Letter;

public interface LetterRepository extends JpaRepository<Letter, Long> {
}
