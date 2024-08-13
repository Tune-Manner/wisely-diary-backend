package tuneandmanner.wiselydiarybackend.music.domain.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
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
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    private Long diarySummaryCode;
    private String clipId;

    public Music(String musicPath, String musicTitle, String musicLyrics, LocalDateTime createdAt, String clipId, Long diarySummaryCode) {
        this.musicPath = musicPath;
        this.musicTitle = musicTitle;
        this.musicLyrics = musicLyrics;
        this.createdAt = createdAt;
        this.clipId = clipId;
        this.diarySummaryCode = diarySummaryCode;
    }

    public static Music create(String musicPath, String title, String lyrics, LocalDateTime createdAt, String clipId, Long diarySummaryCode) {
        return new Music(musicPath, title, lyrics, createdAt, clipId, diarySummaryCode);
    }
}
