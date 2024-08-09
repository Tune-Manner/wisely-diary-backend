package tuneandmanner.wiselydiarybackend.music.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tuneandmanner.wiselydiarybackend.auth.config.SunoApiConfig;
import tuneandmanner.wiselydiarybackend.common.exception.CustomException;
import tuneandmanner.wiselydiarybackend.common.exception.NotFoundException;
import tuneandmanner.wiselydiarybackend.common.exception.ServerInternalException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;
import tuneandmanner.wiselydiarybackend.common.util.TokenUtils;
import tuneandmanner.wiselydiarybackend.music.dto.response.CreateMusicResponse;
import tuneandmanner.wiselydiarybackend.music.dto.response.SunoApiResponse;
import tuneandmanner.wiselydiarybackend.music.dto.response.SunoClipResponse;
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

    public CreateMusicResponse createSong(SunoApiRequest sunoRequest) {
        String token = tokenUtils.addBearerPrefix(sunoApiConfig.getToken());

        log.info("SunoApiRequest: {}", sunoRequest);

        SunoApiResponse response = webClient.post()
                .uri("/api/v2/suno/v3.5/custom/create-lyrics-song")
                .header("Authorization", token)
                .bodyValue(sunoRequest)
                .retrieve()
                .bodyToMono(SunoApiResponse.class)
                .block();

        return CreateMusicResponse.from(response);
    }

    public SunoClipResponse getClipResponse(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Invalid taskId: " + taskId);
        }

        String token = tokenUtils.addBearerPrefix(sunoApiConfig.getToken());

        log.info("Requesting clip URL for task ID: {}", taskId);
        log.info("Using token: {}", token);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/suno/clip/{taskId}")
                        .build(taskId))
                .header("Authorization", token)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> {
                    HttpStatusCode statusCode = response.statusCode();
                    log.error("Error response from Suno API. Status: {}", statusCode);
                    if (statusCode == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(new CustomException(ExceptionCode.UNAUTHORIZED));
                    } else if (statusCode == HttpStatus.FORBIDDEN) {
                        return Mono.error(new CustomException(ExceptionCode.ACCESS_DENIED));
                    } else if (statusCode == HttpStatus.NOT_FOUND) {
                        return Mono.error(new NotFoundException(ExceptionCode.NOT_FOUND_CLIP_URL));
                    } else if (statusCode.is4xxClientError()) {
                        return Mono.error(new CustomException(ExceptionCode.CLIENT_ERROR));
                    } else if (statusCode.is5xxServerError()) {
                        return Mono.error(new ServerInternalException(ExceptionCode.SERVER_ERROR));
                    } else {
                        return Mono.error(new CustomException(ExceptionCode.UNKNOWN_ERROR));
                    }
                })
                .bodyToMono(SunoClipResponse.class)
                .blockOptional()
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND_CLIP_URL));
    }
}
