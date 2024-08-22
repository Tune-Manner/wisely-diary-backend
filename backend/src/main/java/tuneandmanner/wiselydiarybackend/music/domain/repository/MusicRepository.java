package tuneandmanner.wiselydiarybackend.music.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.music.domain.entity.Music;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByMusicCode(Long musicCode);

    Optional<Music> findByDiaryCode(Long diaryCode);

    Optional<Music> findByClipId(String id);

    List<Music> findByDiaryCodeInAndCreatedAtBetween(List<Long> diaryCodes, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
