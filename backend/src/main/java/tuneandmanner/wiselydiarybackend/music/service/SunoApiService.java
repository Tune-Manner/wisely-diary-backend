package tuneandmanner.wiselydiarybackend.music.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
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

    public SunoClipResponse getClipResponse(String clipId) {

        String token = tokenUtils.addBearerPrefix(sunoApiConfig.getToken());

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v2/suno/v3.5/clips")
                        .queryParam("ids", clipId)
                        .build())
                .header("Authorization", token)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .bodyToMono(SunoClipResponse.class)
                .doOnError(error -> log.error("Error occurred while calling Suno API", error))
                .blockOptional()
                .orElseThrow(() -> new NotFoundException(ExceptionCode.NOT_FOUND_CLIP_URL));
    }

    private Mono<Throwable> handleServerError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Server error: {}. Response body: {}", response.statusCode(), body);
                    return Mono.error(new ServerInternalException(ExceptionCode.SERVER_ERROR));
                });
    }

    private Mono<Throwable> handleClientError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("Client error: {}. Response body: {}", response.statusCode(), body);
                    return Mono.error(new CustomException(ExceptionCode.CLIENT_ERROR));
                });
    }
}
