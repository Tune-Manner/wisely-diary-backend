package tuneandmanner.wiselydiarybackend.stt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString
public class WhisperTranscriptionResponse {
	private String text;
}
