package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByMemberCodeAndCreatedAtBetweenAndDiaryStatus(Long memberCode, LocalDateTime start, LocalDateTime end, String status);
}
