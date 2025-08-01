package club.ss220.manager.data.integration.game;

import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdminStatus;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GameApiClient {

    GameServer.Build getBuild();

    Mono<GameServerStatus> getServerStatus(GameServer gameServer);

    Mono<List<String>> getPlayersList(GameServer gameServer);

    Mono<List<OnlineAdminStatus>> getAdminsList(GameServer gameServer);

    Mono<Boolean> sendHostAnnounce(GameServer gameServer, String message);

    Mono<Boolean> sendAdminMessage(GameServer gameServer, String ckey, String message, String adminName);
}
