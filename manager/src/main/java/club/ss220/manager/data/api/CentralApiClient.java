package club.ss220.manager.data.api;

import reactor.core.publisher.Mono;

public interface CentralApiClient {

    Mono<MemberDto> getMemberByCkey(String ckey);

    Mono<MemberDto> getMemberByDiscordId(long discordId);
}
