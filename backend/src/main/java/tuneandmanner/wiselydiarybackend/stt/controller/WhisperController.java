package tuneandmanner.wiselydiarybackend.stt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;
import tuneandmanner.wiselydiarybackend.stt.service.OpenAIClientService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class WhisperController {

	private final OpenAIClientService openAIClientService;

	@PostMapping(value = "/transcription", consumes = "multipart/form-data")
	public WhisperTranscriptionResponse createTranscription(@RequestParam("file") MultipartFile file) {
		// 파일 로그 추가
		try {
			System.out.println("Received file: " + file.getOriginalFilename());
			System.out.println("File size: " + file.getSize() + " bytes");
			Files.copy(file.getInputStream(), Paths.get("received_" + file.getOriginalFilename()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return openAIClientService.createTranscription(file);
	}
}
