package tuneandmanner.wiselydiarybackend.diarysummary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tuneandmanner.wiselydiarybackend.diarysummary.domain.DiarySummary;


@Repository
public interface DiarySummaryRepository extends JpaRepository<DiarySummary, Long> {
//	DiarySummary findByDiaryCode(Long diaryCode);
	Optional<DiarySummary> findByDiarySummaryCode(@Param("diarySummaryCode") Long diarySummaryCode);
	List<DiarySummary> findByDiaryCodeIn(List<Long> diaryCodes);
}