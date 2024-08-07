package tuneandmanner.wiselydiarybackend.cartoon.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
    private Integer cartoonCode;
    private String cartoonPath;
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name="diary_summary_code")
    private DiarySummary diarySummary;

    // 생성자, 빌더 등 필요한 메서드 추가
}