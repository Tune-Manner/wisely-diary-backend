package tuneandmanner.wiselydiarybackend.cartoon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "diary")
@Getter
@Setter
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer diaryCode;

    @Column(columnDefinition = "TEXT")
    private String diaryContents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @ManyToOne
    @JoinColumn(name="member_code")
    private Member member;

    private Integer emotionCode;
    private String diaryStatus;
}
