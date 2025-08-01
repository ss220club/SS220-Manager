package club.ss220.manager.service;

import club.ss220.manager.config.GameConfig;
import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdminStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@AllArgsConstructor
public class GameServerService {

    private final GameConfig gameConfig;
    private final GameBuildStrategyService strategyService;

    public GameServerStatus getServerStatus(String serverName) {
        GameServer gameServer = getServerByName(serverName);
        return getGameApiClient(gameServer).getServerStatus(gameServer).block();
    }

    public Map<GameServer, GameServerStatus> getAllServersStatus() {
        return Flux.fromIterable(gameConfig.getServers())
                .flatMap(server -> Mono.fromCallable(() -> getGameApiClient(server))
                        .onErrorResume(e -> {
                            log.error(e.getMessage(), e);
                            return Mono.empty();
                        })
                        .flatMap(client -> client.getServerStatus(server)
                                .map(status -> Map.entry(server, status))
                        )
                        .onErrorResume(e -> {
                            log.error("Error getting status for server: {}", server.getFullName(), e);
                            return Mono.empty();
                        })
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .block();
    }

    public List<String> getPlayersList(String serverName) {
        GameServer gameServer = getServerByName(serverName);
        return getGameApiClient(gameServer).getPlayersList(gameServer).block();
    }

    public Map<GameServer, List<OnlineAdminStatus>> getAllAdminsList() {
        return Flux.fromIterable(gameConfig.getServers())
                .flatMap(server -> Mono.fromCallable(() -> getGameApiClient(server))
                        .onErrorResume(e -> {
                            log.error(e.getMessage(), e);
                            return Mono.empty();
                        })
                        .flatMap(client -> client.getAdminsList(server)
                                .onErrorResume(e -> {
                                    log.error("Failed to get admins from server: {}", server.getName(),
                                              e);
                                    return Mono.empty();
                                })
                                .map(list -> Map.entry(server, list))
                        )
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .blockOptional()
                .orElseGet(Collections::emptyMap);
    }

    public boolean sendHostAnnounce(String serverName, String message) {
        GameServer gameServer = getServerByName(serverName);
        GameApiClient gameApiClient = getGameApiClient(gameServer);
        return Boolean.TRUE.equals(gameApiClient.sendHostAnnounce(gameServer, message).block());
    }

    public boolean sendAdminMessage(String serverName, String ckey, String message, String adminName) {
        GameServer gameServer = getServerByName(serverName);
        GameApiClient gameApiClient = getGameApiClient(gameServer);
        return Boolean.TRUE.equals(gameApiClient.sendAdminMessage(gameServer, ckey, message, adminName).block());
    }

    private GameServer getServerByName(String serverName) {
        return gameConfig.getServerByName(serverName)
                .orElseThrow(() -> new NoSuchElementException("Unknown server: " + serverName));
    }

    private GameApiClient getGameApiClient(GameServer gameServer) {
        return strategyService.getGameApiClient(gameServer.getBuild());
    }
}
