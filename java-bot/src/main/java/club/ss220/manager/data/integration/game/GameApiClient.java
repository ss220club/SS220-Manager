package club.ss220.manager.data.integration.game;

import club.ss220.manager.model.GameServer;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface GameApiClient {
    
    Mono<ServerStatusDto> getServerStatus(String serverName);

    Mono<Map<GameServer, ServerStatusDto>> getAllServersStatus();
    
    Mono<List<PlayerStatusDto>> getPlayersList(String serverName);

    Mono<Map<GameServer, List<AdminStatusDto>>> getAllAdminsList();
    
    Mono<Boolean> sendHostAnnounce(String serverName, String message);

    Mono<Boolean> sendAdminMessage(String serverName, String ckey, String message, String adminName);
}
