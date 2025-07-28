package club.ss220.manager.data.api.impl;

import club.ss220.manager.data.api.CentralApiClient;
import club.ss220.manager.data.api.PlayerDto;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Primary
@Profile("test")
public class TestCentralApiClient implements CentralApiClient {

    private final List<PlayerDto> players = List.of(
            new PlayerDto(1, "testckey1", 1L),
            new PlayerDto(2, "testckey2", 2L)
    );

    @Override
    public Mono<PlayerDto> getPlayerByCkey(String ckey) {
        return Mono.justOrEmpty(players.stream()
                                        .filter(p -> p.getCkey().equals(ckey))
                                        .findFirst());
    }

    @Override
    public Mono<PlayerDto> getPlayerByDiscordId(long discordId) {
        return Mono.justOrEmpty(players.stream()
                                        .filter(p -> p.getDiscordId() == discordId)
                                        .findFirst());
    }
}
