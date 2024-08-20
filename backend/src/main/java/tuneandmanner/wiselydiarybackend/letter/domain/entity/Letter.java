package tuneandmanner.wiselydiarybackend.letter.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "letter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "letter_code")
    private Long letterCode;
    @Column(name = "letter_contents", columnDefinition = "TEXT")
    private String letterContents;
    @Column(name = "diary_code")
    private Long diaryCode;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Letter(Long letterCode, String letterContents, LocalDateTime createdAt, Long diaryCode) {
        this.letterCode = letterCode;
        this.letterContents = letterContents;
        this.createdAt = createdAt;
        this.diaryCode = diaryCode;
    }

    public static Letter of(
            final Long letterCode, final String letterContents, final LocalDateTime createdAt, final Long diaryCode
            ) {
        return new Letter(
                letterCode,
                letterContents,
                createdAt,
                diaryCode
        );
    }
}
