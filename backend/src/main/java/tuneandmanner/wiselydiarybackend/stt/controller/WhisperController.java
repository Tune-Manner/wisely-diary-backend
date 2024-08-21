package tuneandmanner.wiselydiarybackend.stt.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;
import tuneandmanner.wiselydiarybackend.stt.service.OpenAIClientService;
import java.nio.file.StandardCopyOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class WhisperController {

	private final OpenAIClientService openAIClientService;

	@PostMapping(value = "/transcription", consumes = "multipart/form-data")
	public ResponseEntity<WhisperTranscriptionResponse> createTranscription(@RequestParam("file") MultipartFile file) {
		try {
			System.out.println("Received file: " + file.getOriginalFilename());
			System.out.println("File size: " + file.getSize() + " bytes");

			// 파일 덮어쓰기 옵션 추가
			Files.copy(file.getInputStream(), Paths.get("received_" + file.getOriginalFilename()),
				StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 에러 발생 시 500 에러 반환
		}

		WhisperTranscriptionResponse response = openAIClientService.createTranscription(file);

		// 응답을 UTF-8로 인코딩하여 반환
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON_UTF8)  // 응답 Content-Type을 UTF-8로 설정
			.body(response);
	}
}
