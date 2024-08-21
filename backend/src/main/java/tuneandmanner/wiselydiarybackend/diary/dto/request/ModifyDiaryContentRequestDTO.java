package tuneandmanner.wiselydiarybackend.diary.dto.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ModifyDiaryContentRequestDTO {
	private Long diaryCode;
	private String diaryContent;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String memberId;
	private Integer emotionCode;
	private String diaryStatus;

}
