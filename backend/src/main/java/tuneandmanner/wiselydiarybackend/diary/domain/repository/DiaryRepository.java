package tuneandmanner.wiselydiarybackend.diary.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
