package tuneandmanner.wiselydiarybackend.stt.controller;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
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
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

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
		logger.info("Received file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

		// 파일 형식 검증
		String[] allowedExtensions = {"mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm"};
		String fileExtension = getFileExtension(file.getOriginalFilename());
		if (!Arrays.asList(allowedExtensions).contains(fileExtension.toLowerCase())) {
			return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
					.body("Unsupported file format. Allowed formats are: " + String.join(", ", allowedExtensions));
		}

		try {
			File processedFile;
			if (file.getSize() > 25 * 1024 * 1024) { // If file is larger than 25MB
				processedFile = compressAudioFile(file);
				logger.info("Compressed file size: {} bytes", processedFile.length());
			} else {
				processedFile = convertMultipartFileToFile(file);
			}

			// Use processedFile for transcription
			WhisperTranscriptionResponse response = openAIClientService.createTranscription(new FileSystemResource(processedFile));

			processedFile.delete(); // Clean up

			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(response);

		} catch (FeignException e) {
			logger.error("Feign client error: ", e);
			if (e.status() == 413) {
				return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
						.body("File size too large for OpenAI API. Maximum allowed size is 25MB.");
			} else if (e.status() == 429) {
				return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
						.body("Too many requests to OpenAI API. Please try again later.");
			} else {
				return ResponseEntity.status(e.status())
						.body("Error calling OpenAI API: " + e.getMessage());
			}
		} catch (IOException e) {
			logger.error("Error processing file: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error processing the uploaded file");
		} catch (Exception e) {
			logger.error("Unexpected error: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An unexpected error occurred: " + e.getMessage());
		}
	}

	private String getFileExtension(String filename) {
		return Optional.ofNullable(filename)
				.filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1))
				.orElse("");
	}

	private File compressAudioFile(MultipartFile file) throws Exception {
		File inputFile = convertMultipartFileToFile(file);
		File outputFile = new File(inputFile.getParent(), "compressed_" + inputFile.getName());

		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("libmp3lame");
		audio.setBitRate(32000); // Adjust bitrate for desired quality/size
		audio.setChannels(1);
		audio.setSamplingRate(22050);

		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setOutputFormat("mp3"); // 여기를 setFormat에서 setOutputFormat으로 변경
		attrs.setAudioAttributes(audio);

		Encoder encoder = new Encoder();
		encoder.encode(new MultimediaObject(inputFile), outputFile, attrs);

		inputFile.delete(); // Delete the original file
		return outputFile;
	}

	private File convertMultipartFileToFile(MultipartFile file) throws IOException {
		File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
		file.transferTo(convFile);
		return convFile;
	}
}