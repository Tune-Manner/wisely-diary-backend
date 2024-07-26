package tuneandmanner.wiselydiarybackend.stt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.stt.client.OpenAIClient;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAIClientConfig;

@RequiredArgsConstructor
@Service
public class OpenAIClientService {

	private final OpenAIClient openAIClient;
	private final OpenAIClientConfig openAIClientConfig;

	public WhisperTranscriptionResponse createTranscription(MultipartFile file) {
		String model = openAIClientConfig.getAudioModel();
		return openAIClient.createTranscription(file, model);
	}
}
