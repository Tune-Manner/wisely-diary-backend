package tuneandmanner.wiselydiarybackend.stt.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tuneandmanner.wiselydiarybackend.auth.config.OpenAIClientConfig;
import tuneandmanner.wiselydiarybackend.stt.client.OpenAIClient;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;

@Service
public class OpenAIClientService {

	private final OpenAIClient openAIClient;
	private final OpenAIClientConfig openAIClientConfig;

	public OpenAIClientService(OpenAIClient openAIClient, OpenAIClientConfig openAIClientConfig) {
		this.openAIClient = openAIClient;
		this.openAIClientConfig = openAIClientConfig;
	}

	public WhisperTranscriptionResponse createTranscription(MultipartFile file) {
		String model = openAIClientConfig.getAudioModel();
		return openAIClient.createTranscription(file, model);
	}
}
