package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.DiarySummary;

public interface DiarySummaryRepository extends JpaRepository<DiarySummary, Long> {
    DiarySummary findByDiaryCode(Long diaryCode);
}