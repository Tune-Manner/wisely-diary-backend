package tuneandmanner.wiselydiarybackend.cartoon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "diary_summary")
@Getter
@Setter
public class DiarySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer diarySummaryCode;

    @Column(columnDefinition = "TEXT")
    private String diarySummaryContents;

    @ManyToOne
    @JoinColumn(name="diary_code")
    private Diary diary;
}
