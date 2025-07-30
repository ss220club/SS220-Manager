package club.ss220.manager.data.integration.game;

import club.ss220.manager.data.integration.game.impl.paradise.AdminStatusDto;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GameApiClient<S extends GameServerStatus> {

    Mono<S> getServerStatus(GameServer gameServer);

    Mono<List<String>> getPlayersList(GameServer gameServer);

    Mono<List<AdminStatusDto>> getAdminsList(GameServer gameServer);

    Mono<Boolean> sendHostAnnounce(GameServer gameServer, String message);

    Mono<Boolean> sendAdminMessage(GameServer gameServer, String ckey, String message, String adminName);
}
