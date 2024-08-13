package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    Optional<Diary> findByMemberIdAndCreatedAtBetween(String memberId, LocalDateTime start, LocalDateTime end);
}