package club.ss220.manager.data.api.impl;

import club.ss220.manager.data.api.CentralApiClient;
import club.ss220.manager.data.api.PlayerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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

    public Mono<PlayerDto> getPlayerByCkey(String ckey) {
        return webClient.get()
                .uri("/players/ckey/{ckey}", ckey)
                .retrieve()
                .bodyToMono(PlayerDto.class)
                .doOnError(e -> log.error("Error getting player by ckey: {}", ckey, e));
    }

    public Mono<PlayerDto> getPlayerByDiscordId(long discordId) {
        return webClient.get()
                .uri("/players/discord/{discordId}", discordId)
                .retrieve()
                .bodyToMono(PlayerDto.class)
                .doOnError(e -> log.error("Error getting player by Discord ID: {}", discordId, e));
    }
}
