package tuneandmanner.wiselydiarybackend.stt.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAiConfig;
import tuneandmanner.wiselydiarybackend.common.config.FeignConfig;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAIClientConfig;

@FeignClient(name = "openai-service", url = "https://api.openai.com/v1", configuration = {OpenAIClientConfig.class, FeignConfig.class})
public interface OpenAIClient {
	@PostMapping(value = "/audio/transcriptions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	WhisperTranscriptionResponse createTranscription(
			@RequestPart("file") MultipartFile file,
			@RequestPart("model") String model
	);
}