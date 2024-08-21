package tuneandmanner.wiselydiarybackend.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class OpenAIClientConfig {

	@Value("${spring.ai.openai.api-key}")
	private String apiKey;

//	@Value("${spring.ai.openai.audio-model}")
	@Value("whisper-1")
	private String audioModel;

	@Bean
	public String getApiKey() {
		return apiKey;
	}

	@Bean
	public String getAudioModel() {
		return audioModel;
	}

	@Bean
	public RequestInterceptor requestInterceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				template.header("Authorization", "Bearer " + apiKey);
			}
		};
	}
}
