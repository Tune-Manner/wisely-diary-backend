package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.DiarySummary;

import java.util.Optional;

public interface DiarySummaryRepository extends JpaRepository<DiarySummary, Long> {
    DiarySummary findByDiaryCode(Long diaryCode);

    Optional<DiarySummary> findByDiarySummaryCode(@Param("diarySummaryCode") Long diarySummaryCode);
}