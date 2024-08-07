package tuneandmanner.wiselydiarybackend.stt.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString
public class TranscriptionRequest {
	private MultipartFile file;
}
