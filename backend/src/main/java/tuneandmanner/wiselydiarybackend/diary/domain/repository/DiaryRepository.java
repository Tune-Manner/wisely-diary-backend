package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 특정 사용자가 지정한 시간 범위 내에 작성한 일기를 조회
    Optional<Diary> findByDiaryCode(Long diaryCode);

    Optional<Diary> findByMemberIdAndCreatedAtBetween(String memberId, LocalDateTime start, LocalDateTime end);

    // 특정 사용자가 지정한 시간 범위 내에서 상태가 특정 값인 일기들을 조회
    List<Diary> findByMemberIdAndCreatedAtBetweenAndDiaryStatus(String memberId, LocalDateTime startOfDay, LocalDateTime endOfDay, String exist);

    // 특정 사용자가 지정한 시간 범위 내에서 작성한 모든 일기를 조회
    List<Diary> findAllByMemberIdAndCreatedAtBetween(String memberId, LocalDateTime start, LocalDateTime end);

    // 특정 사용자가 지정한 시간 범위 내에서 상태가 특정 값인 일기들을 작성 날짜 기준 내림차순으로 조회
    List<Diary> findByMemberIdAndCreatedAtBetweenAndDiaryStatusOrderByCreatedAtDesc(
            String memberId, LocalDateTime startOfMonth, LocalDateTime endOfMonth, String status);

    // 특정 사용자가 지정한 시간 범위 내에서 상태가 특정 값인 일기들 중 작성 날짜 기준 내림차순으로 조회한 것들 중 가장 최근의 1개 조회
    Optional<Diary> findFirstByMemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String memberId, LocalDateTime start, LocalDateTime end);
}
