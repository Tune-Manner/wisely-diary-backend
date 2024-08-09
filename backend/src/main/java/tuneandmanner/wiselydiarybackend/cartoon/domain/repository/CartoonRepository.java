package tuneandmanner.wiselydiarybackend.cartoon.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartoonRepository extends JpaRepository<Cartoon, Long> {
    List<Cartoon> findByDiarySummaryCodeInAndCreatedAtBetween(List<Long> diarySummaryCodes, LocalDateTime start, LocalDateTime end);
}