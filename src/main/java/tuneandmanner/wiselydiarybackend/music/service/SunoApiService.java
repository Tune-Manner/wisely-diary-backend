package tuneandmanner.wiselydiarybackend.music.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tuneandmanner.wiselydiarybackend.auth.config.SunoApiConfig;
import tuneandmanner.wiselydiarybackend.common.util.TokenUtils;
import tuneandmanner.wiselydiarybackend.music.dto.request.SunoApiRequest;

@Slf4j
@Service
public class SunoApiService {

    private final WebClient webClient;
    private final SunoApiConfig sunoApiConfig;
    private final TokenUtils tokenUtils;

    public SunoApiService(SunoApiConfig sunoApiConfig, TokenUtils tokenUtils, WebClient.Builder webClientBuilder) {
        this.sunoApiConfig = sunoApiConfig;
        this.tokenUtils = tokenUtils;
        this.webClient = webClientBuilder.baseUrl("https://api.sunoapi.com").build();
    }

    public void createSong(SunoApiRequest sunoRequest) {
        String token = tokenUtils.addBearerPrefix(sunoApiConfig.getToken());

        log.info(sunoRequest.toString());

        webClient.post()
                .uri("/api/v1/suno/create")
                .header("Authorization", token)
                .bodyValue(sunoRequest)
                .retrieve()
                .toBodilessEntity() // 응답 본문이 없는 경우 처리
                .block();
    }
}
