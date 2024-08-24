package tuneandmanner.wiselydiarybackend.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class OpenAIClientConfig {

	private final OpenAiConfig openAiConfig;

	public OpenAIClientConfig(OpenAiConfig openAiConfig) {
		this.openAiConfig = openAiConfig;
	}

	@Bean
	public RequestInterceptor requestInterceptor() {
		return template -> {
			template.header("Authorization", "Bearer " + openAiConfig.getApiKey());
		};
	}

	@Bean
	public String getAudioModel() {
		return openAiConfig.getAudioModel();
	}

	@Bean
	public String getBaseUrl() {
		return openAiConfig.getUrls().getBaseUrl();
	}

	@Bean
	public String getCreateTranscriptionUrl() {
		return openAiConfig.getUrls().getCreateTranscriptionUrl();
	}
}
