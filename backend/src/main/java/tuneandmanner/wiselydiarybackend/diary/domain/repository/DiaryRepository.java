package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    Optional<Diary> findByMemberIdAndCreatedAtBetween(String memberId, LocalDateTime start, LocalDateTime end);

    List<Diary> findByMemberIdAndCreatedAtBetweenAndDiaryStatus(String memberId, LocalDateTime startOfDay, LocalDateTime endOfDay, String exist);

    // 새로운 메서드: 특정 기간 동안 특정 사용자의 모든 다이어리 항목을 리스트로 반환
    List<Diary> findAllByMemberIdAndCreatedAtBetween(String memberId, LocalDateTime start, LocalDateTime end);
}