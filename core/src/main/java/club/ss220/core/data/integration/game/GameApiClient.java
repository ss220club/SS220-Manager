package club.ss220.core.data.integration.game;

import club.ss220.core.model.GameServer;
import club.ss220.core.model.GameServerStatus;
import club.ss220.core.model.OnlineAdminStatus;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GameApiClient {

    Mono<GameServerStatus> getServerStatus(GameServer gameServer);

    Mono<List<String>> getPlayersList(GameServer gameServer);

    Mono<List<OnlineAdminStatus>> getAdminsList(GameServer gameServer);

    Mono<Boolean> sendHostAnnounce(GameServer gameServer, String message);

    Mono<Boolean> sendAdminMessage(GameServer gameServer, String ckey, String message, String adminName);
}
