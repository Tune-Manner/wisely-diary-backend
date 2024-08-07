package tuneandmanner.wiselydiarybackend.cartoon.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface CartoonRepository extends JpaRepository<Cartoon, Integer> {
    @Query("SELECT c FROM Cartoon c " +
            "JOIN DiarySummary ds ON c.diarySummary.diarySummaryCode = ds.diarySummaryCode " +
            "JOIN Diary d ON ds.diary.diaryCode = d.diaryCode " +
            "WHERE c.createdAt BETWEEN :startOfDay AND :endOfDay " +
            "AND d.member.memberId = :memberId")
    List<Cartoon> findByCreatedAtBetweenAndMemberId(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("memberId") String memberId
    );
}