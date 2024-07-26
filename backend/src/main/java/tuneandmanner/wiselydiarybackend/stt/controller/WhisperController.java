package tuneandmanner.wiselydiarybackend.stt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;
import tuneandmanner.wiselydiarybackend.stt.service.OpenAIClientService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class
WhisperController {

	private final OpenAIClientService openAIClientService;

	@PostMapping(value = "/transcription", consumes = "multipart/form-data")
	public WhisperTranscriptionResponse createTranscription(@RequestParam("file") MultipartFile file) {
		return openAIClientService.createTranscription(file);
	}
}
