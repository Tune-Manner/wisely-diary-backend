package tuneandmanner.wiselydiarybackend.stt.controller;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class WhisperController {

	private static final Logger logger = LoggerFactory.getLogger(WhisperController.class);
	private final OpenAIClientService openAIClientService;

	public WhisperController(OpenAIClientService openAIClientService) {
		this.openAIClientService = openAIClientService;
	}

	@PostMapping(value = "/transcription", consumes = "multipart/form-data")
	public ResponseEntity<?> createTranscription(@RequestParam("file") MultipartFile file) {
		logger.info("Received file: {}", file.getOriginalFilename());
		logger.info("File size: {} bytes", file.getSize());

		Path tempFile = null;
		try {
			// 시스템의 임시 디렉토리 사용
			String tempDir = System.getProperty("java.io.tmpdir");
			tempFile = Paths.get(tempDir, "received_" + file.getOriginalFilename());

			// 파일 덮어쓰기
			Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

			logger.info("File saved to: {}", tempFile);

			// OpenAI 서비스 호출
			WhisperTranscriptionResponse response = openAIClientService.createTranscription(file);

			// 응답 반환
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(response);

		} catch (FeignException e) {
			logger.error("Feign client error: ", e);
			if (e.status() == 413) {
				return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
						.body("File size too large for OpenAI API");
			}
			return ResponseEntity.status(e.status())
					.body("Error calling OpenAI API: " + e.getMessage());
		} catch (IOException e) {
			logger.error("Error processing file: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing the uploaded file");
		} catch (Exception e) {
			logger.error("Unexpected error: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred");
		} finally {
			// 임시 파일 삭제
			if (tempFile != null) {
				try {
					boolean deleted = Files.deleteIfExists(tempFile);
					if (deleted) {
						logger.info("Temporary file successfully deleted: {}", tempFile);
					} else {
						logger.warn("Temporary file did not exist or could not be deleted: {}", tempFile);
					}
				} catch (IOException e) {
					logger.warn("Failed to delete temporary file: {}", tempFile, e);
				}
			}
		}
	}
}