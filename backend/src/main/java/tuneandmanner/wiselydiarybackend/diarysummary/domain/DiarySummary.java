package tuneandmanner.wiselydiarybackend.diarysummary.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tuneandmanner.wiselydiarybackend.diary.domain.entity.Diary;

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



	@ManyToOne
	@JoinColumn(name="diary_code")
	private Diary diary;

//	@Builder
//	public DiarySummary(String diarySummaryContents, Diary diary) {
//		this.diarySummaryContents = diarySummaryContents;
//		this.diaryCode = diaryCode;
//	}
//
//	public DiarySummary updateContents(String newContents) {
//		return DiarySummary.builder()
//			.diarySummaryContents(newContents)
//			.diaryCode(this.diaryCode)
//			.build();
//	}
}