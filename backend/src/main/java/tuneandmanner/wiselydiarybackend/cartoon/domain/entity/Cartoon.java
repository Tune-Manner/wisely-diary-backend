package tuneandmanner.wiselydiarybackend.cartoon.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tuneandmanner.wiselydiarybackend.diarysummary.domain.DiarySummary;

import java.time.LocalDateTime;

@Entity
@Table(name = "cartoon")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
public class Cartoon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    @Column(name = "cartoon_code")
    private Long cartoonCode;
    @Column(name = "cartoon_path", nullable = false)
=======
    private Long cartoonCode;
>>>>>>> 532c25d9a65f96b956b9ebec34d1984b36bcb1d4
    private String cartoonPath;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "diary_summary_code", nullable = false)
    private Long diarySummaryCode;

    // 생성자, 빌더 등 필요한 메서드 추가
}