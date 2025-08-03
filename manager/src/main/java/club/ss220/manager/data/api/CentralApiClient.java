package club.ss220.manager.data.api;

import reactor.core.publisher.Mono;

public interface CentralApiClient {

    Mono<UserDto> getUserByCkey(String ckey);

    Mono<UserDto> getUserByDiscordId(long discordId);
}
