package club.ss220.manager.api.central;

import club.ss220.manager.api.central.model.PlayerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CentralApiClient {

    private final WebClient webClient;

    public CentralApiClient(@Value("${api.central.endpoint}") String baseUrl,
                           @Value("${api.central.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    public Mono<PlayerDTO> getPlayerByCkey(String ckey) {
        return webClient.get()
                .uri("/players/ckey/{ckey}", ckey)
                .retrieve()
                .bodyToMono(PlayerDTO.class)
                .doOnError(e -> log.error("Error getting player by ckey: {}", ckey, e));
    }

    public Mono<PlayerDTO> getPlayerByDiscordId(long discordId) {
        return webClient.get()
                .uri("/players/discord/{discordId}", discordId)
                .retrieve()
                .bodyToMono(PlayerDTO.class)
                .doOnError(e -> log.error("Error getting player by Discord ID: {}", discordId, e));
    }
}
