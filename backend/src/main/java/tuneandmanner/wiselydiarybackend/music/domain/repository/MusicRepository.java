package tuneandmanner.wiselydiarybackend.music.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;

public interface MusicRepository extends JpaRepository<Music, Long> {
}
