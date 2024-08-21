package tuneandmanner.wiselydiarybackend.diary.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
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

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "emotion_code", nullable = false)
    private Integer emotionCode;

    @Column(name = "diary_status", length = 20)
    private String diaryStatus = "EXIST";
}