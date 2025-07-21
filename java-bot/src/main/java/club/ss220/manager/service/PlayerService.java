package club.ss220.manager.service;

import club.ss220.manager.api.central.CentralApiClient;
import club.ss220.manager.api.central.model.PlayerDTO;
import club.ss220.manager.db.paradise.entity.Player;
import club.ss220.manager.db.paradise.repository.PlayerRepository;
import club.ss220.manager.util.CkeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PlayerService {

    private final CentralApiClient centralApiClient;
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(CentralApiClient centralApiClient, PlayerRepository playerRepository) {
        this.centralApiClient = centralApiClient;
        this.playerRepository = playerRepository;
    }

    public Optional<Player> getPlayerByCkey(String ckey) {
        String sanitizedCkey = CkeyUtils.sanitizeCkey(ckey);
        return playerRepository.findByCkey(sanitizedCkey);
    }

    public Optional<Player> getPlayerByDiscordId(Long discordId) {
        return centralApiClient.getPlayerByDiscordId(discordId)
                .blockOptional()
                .map(PlayerDTO::getCkey)
                .flatMap(playerRepository::findByCkey);
    }
}
