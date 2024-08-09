package tuneandmanner.wiselydiarybackend.diary.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
<<<<<<< HEAD
=======
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Member;


>>>>>>> 532c25d9a65f96b956b9ebec34d1984b36bcb1d4
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


<<<<<<< HEAD
    @Column(name = "member_code", nullable = false)
    private Long memberCode;
=======
    @ManyToOne
    @JoinColumn(name="member_code")
    private Member member;
>>>>>>> 532c25d9a65f96b956b9ebec34d1984b36bcb1d4

    @Column(name = "emotion_code", nullable = false)
    private Integer emotionCode;

    @Column(name = "diary_status", length = 20)
    private String diaryStatus = "EXIST";

//    @Builder
//    public Diary(
//            Long diaryCode,
//            String diaryContents,
//            LocalDateTime createdAt,
//            LocalDateTime updatedAt,
//            Member member,
//            Integer emotionCode,
//            String diaryStatus
//    ) {
//        this.diaryCode = diaryCode;
//        this.diaryContents = diaryContents;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
//        this.memberCode = memberCode;
//        this.emotionCode = emotionCode;
//        this.diaryStatus = diaryStatus;
//    }
}