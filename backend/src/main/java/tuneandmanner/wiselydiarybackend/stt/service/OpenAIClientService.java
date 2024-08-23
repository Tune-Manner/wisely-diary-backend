package tuneandmanner.wiselydiarybackend.stt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tuneandmanner.wiselydiarybackend.auth.config.OpenAIClientConfig;
import tuneandmanner.wiselydiarybackend.stt.client.OpenAIClient;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;

@Service
public class OpenAIClientService {

	private static final Logger logger = LoggerFactory.getLogger(OpenAIClientService.class);

	private final OpenAIClient openAIClient;
	private final OpenAIClientConfig openAIClientConfig;

	public OpenAIClientService(OpenAIClient openAIClient, OpenAIClientConfig openAIClientConfig) {
		this.openAIClient = openAIClient;
		this.openAIClientConfig = openAIClientConfig;
	}

	public WhisperTranscriptionResponse createTranscription(MultipartFile file) {
		String model = openAIClientConfig.getAudioModel();
		logger.info("Calling OpenAI API with model: {}", model);
		logger.info("File name: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());

		try {
			WhisperTranscriptionResponse response = openAIClient.createTranscription(file, model);
			logger.info("Transcription completed successfully");
			return response;
		} catch (Exception e) {
			logger.error("Error during transcription: ", e);
			throw e; // 상위 레벨에서 처리할 수 있도록 예외를 다시 던집니다.
		}
	}
}
