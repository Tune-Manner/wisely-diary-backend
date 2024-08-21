package tuneandmanner.wiselydiarybackend.diarysummary.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DiarySummary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "diary_summary_code")
	private Long diarySummaryCode;

	@Column(name = "diary_summary_contents", nullable = false)
	private String diarySummaryContents;

	@Column(name = "diary_code", nullable = false)
	private Long diaryCode;

	@Builder
	public DiarySummary(String diarySummaryContents, Long diaryCode) {
		this.diarySummaryContents = diarySummaryContents;
		this.diaryCode = diaryCode;
	}

	public DiarySummary updateContents(String newContents) {
		return DiarySummary.builder()
				.diarySummaryContents(newContents)
				.diaryCode(this.diaryCode)
				.build();
	}
}