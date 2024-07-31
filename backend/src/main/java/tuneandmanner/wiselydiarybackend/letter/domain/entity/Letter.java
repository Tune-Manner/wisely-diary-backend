package tuneandmanner.wiselydiarybackend.letter.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
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
    private Long letterCode;
    private String letterContents;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private Long diarySummaryCode;

    private Letter(Long letterCode, String letterContents, LocalDateTime createdAt, Long diarySummaryCode) {
        this.letterCode = letterCode;
        this.letterContents = letterContents;
        this.createdAt = createdAt;
        this.diarySummaryCode = diarySummaryCode;
    }

    public static Letter of(
            final Long letterCode, final Long diarySummaryCode,
            final String letterContents, final LocalDateTime createdAt
    ) {
        return new Letter(
                letterCode,
                letterContents,
                createdAt,
                diarySummaryCode
        );
    }
}
