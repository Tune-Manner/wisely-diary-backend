package tuneandmanner.wiselydiarybackend.cartoon.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.DiarySummary;

public interface DiarySummaryRepository extends JpaRepository<DiarySummary, Integer> {
}
