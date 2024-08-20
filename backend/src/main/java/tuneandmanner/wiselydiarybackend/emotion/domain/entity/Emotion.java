package tuneandmanner.wiselydiarybackend.emotion.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "emotion")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
public class Emotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emotion_code")
    private Integer emotionCode;
    @Column(name = "emotion_type", nullable = false)
    private String emotionType;
    // 생성자, 빌더 등 필요한 메서드 추가
}
