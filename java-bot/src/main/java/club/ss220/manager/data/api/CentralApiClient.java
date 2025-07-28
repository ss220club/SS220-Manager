package club.ss220.manager.data.api;

import reactor.core.publisher.Mono;

public interface CentralApiClient {

    Mono<PlayerDto> getPlayerByCkey(String ckey);

    Mono<PlayerDto> getPlayerByDiscordId(long discordId);
}
