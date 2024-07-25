package tuneandmanner.wiselydiarybackend.music.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "music")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long musicCode;
    private String musicPath;
    private String musicTitle;
    private String musicLyrics;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private Long diarySummaryCode;
    private String taskId;

    public Music(String musicPath, String musicTitle, String musicLyrics, String taskId) {
        this.musicPath = musicPath;
        this.musicTitle = musicTitle;
        this.musicLyrics = musicLyrics;
        this.createdAt = LocalDateTime.now();
        this.diarySummaryCode = 1L;
        this.taskId = taskId;
    }

    public static Music create(String musicPath, String musicTitle, String musicLyrics, String taskId) {
        return new Music(musicPath, musicTitle, musicLyrics, taskId);
    }
}
