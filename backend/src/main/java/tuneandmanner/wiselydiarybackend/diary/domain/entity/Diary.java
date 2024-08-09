package tuneandmanner.wiselydiarybackend.diary.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_code")
    private Long diaryCode;

    @Column(name = "diary_contents", nullable = false)
    private String diaryContents;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "member_code")
    private Long memberCode;

    @Column(name = "emotion_code", nullable = false)
    private Integer emotionCode;

    @Column(name = "diary_status", length = 20)
    private String diaryStatus = "EXIST";

    @Builder
    public Diary(
            Long diaryCode,
            String diaryContents,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long memberCode,
            Integer emotionCode,
            String diaryStatus
    ) {
        this.diaryCode = diaryCode;
        this.diaryContents = diaryContents;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.memberCode = memberCode;
        this.emotionCode = emotionCode;
        this.diaryStatus = diaryStatus;
    }
}