package tuneandmanner.wiselydiarybackend.music.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;

import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByTaskId(@Param("taskId") String taskId);
}
