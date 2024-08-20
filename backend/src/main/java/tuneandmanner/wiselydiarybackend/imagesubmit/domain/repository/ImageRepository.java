package tuneandmanner.wiselydiarybackend.imagesubmit.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.entity.Image;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByDiaryCode(Long diaryCode);
}
