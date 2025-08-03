package club.ss220.manager.data.api.impl;

import club.ss220.manager.data.api.CentralApiClient;
import club.ss220.manager.data.api.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CentralApiClientImpl implements CentralApiClient {

    private final WebClient webClient;

    public CentralApiClientImpl(@Value("${application.api.central.endpoint}") String baseUrl,
                                @Value("${application.api.central.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    public Mono<UserDto> getUserByCkey(String ckey) {
        return webClient.get()
                .uri("/players/ckey/{ckey}", ckey)
                .retrieve()
                .bodyToMono(UserDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, _ -> {
                    log.info("User with ckey {} not found", ckey);
                    return Mono.empty();
                })
                .doOnError(e -> log.error("Error getting player by ckey: {}", ckey, e));
    }

    public Mono<UserDto> getUserByDiscordId(long discordId) {
        return webClient.get()
                .uri("/players/discord/{discordId}", discordId)
                .retrieve()
                .bodyToMono(UserDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, _ -> {
                    log.info("User with Discord ID {} not found", discordId);
                    return Mono.empty();
                })
                .doOnError(e -> log.error("Error getting player by Discord ID: {}", discordId, e));
    }
}
