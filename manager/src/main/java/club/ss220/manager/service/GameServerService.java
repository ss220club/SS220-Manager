package club.ss220.manager.service;

import club.ss220.manager.data.integration.game.AdminStatusDto;
import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.data.mapper.Mappers;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdmin;
import club.ss220.manager.model.OnlinePlayer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameServerService {

    private final GameApiClient gameApiClient;
    private final Mappers mappers;

    public GameServerService(GameApiClient gameApiClient, Mappers mappers) {
        this.gameApiClient = gameApiClient;
        this.mappers = mappers;
    }

    public GameServerStatus getServerStatus(String serverName) {
        return gameApiClient.getServerStatus(serverName)
                .map(mappers::toGameServerStatus)
                .block();
    }

    public Map<GameServer, GameServerStatus> getAllServersStatus() {
        return gameApiClient.getAllServersStatus()
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> mappers.toGameServerStatus(entry.getValue()),
                                (v1, v2) -> v1)))
                .block();
    }

    public List<OnlinePlayer> getPlayersList(String serverName) {
        return gameApiClient.getPlayersList(serverName)
                .map(players -> players.stream().map(mappers::toOnlinePlayer).toList())
                .block();
    }

    public Map<GameServer, List<OnlineAdmin>> getAllAdminsList() {
        Function<Map<GameServer, List<AdminStatusDto>>, Map<GameServer, List<OnlineAdmin>>> applyMappers =
                serverAdminsMap -> serverAdminsMap.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream()
                                        .map(mappers::toOnlineAdmin)
                                        .toList()
                        ));

        return gameApiClient.getAllAdminsList()
                .map(applyMappers)
                .block();
    }

    public boolean sendHostAnnounce(String serverName, String message) {
        return Boolean.TRUE.equals(gameApiClient.sendHostAnnounce(serverName, message).block());
    }

    public boolean sendAdminMessage(String serverName, String ckey, String message, String adminName) {
        return Boolean.TRUE.equals(gameApiClient.sendAdminMessage(serverName, ckey, message, adminName).block());
    }
}
