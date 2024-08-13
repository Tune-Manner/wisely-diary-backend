package tuneandmanner.wiselydiarybackend.diary.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.DiarySummary;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="diary")
@Getter
@Setter
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_code")
    private Integer diaryCode;

    @Column(name = "diary_contents")
    private String diaryContents;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "emotion_code")
    private Integer emotionCode;

    @Column(name = "diary_status")
    private String diaryStatus;

}
