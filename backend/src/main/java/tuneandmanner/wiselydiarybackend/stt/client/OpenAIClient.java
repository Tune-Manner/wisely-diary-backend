package tuneandmanner.wiselydiarybackend.stt.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.common.config.FeignConfig;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAIClientConfig;

@FeignClient(name = "openai-service", url = "${spring.ai.openai.urls.base-url}", configuration = {OpenAIClientConfig.class, FeignConfig.class})
public interface OpenAIClient {
	@PostMapping(value = "${spring.ai.openai.urls.create-transcription-url}", consumes = "multipart/form-data")
	WhisperTranscriptionResponse createTranscription(@RequestPart("file") MultipartFile file, @RequestPart("model") String model);
}