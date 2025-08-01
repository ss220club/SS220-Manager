package club.ss220.manager.service;

import club.ss220.manager.data.api.impl.CentralApiClientImpl;
import club.ss220.manager.data.api.PlayerDto;
import club.ss220.manager.data.db.game.paradise.repository.ParadisePlayerRepository;
import club.ss220.manager.data.mapper.Mappers;
import club.ss220.manager.model.Player;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PlayerService {

    private final CentralApiClientImpl centralApiClient;
    private final ParadisePlayerRepository paradisePlayerRepository;
    private final Mappers mappers;

    public Optional<Player> getPlayerByCkey(String ckey) {
        Optional<PlayerDto> playerDtoOptional = centralApiClient.getPlayerByCkey(ckey).blockOptional();
        return playerDtoOptional.flatMap(playerDto -> paradisePlayerRepository.findByCkey(playerDto.getCkey())
                .map(paradisePlayer -> mappers.toPlayer(playerDto, paradisePlayer)));
    }

    public Optional<Player> getPlayerByDiscordId(Long discordId) {
        Optional<PlayerDto> playerDtoOptional = centralApiClient.getPlayerByDiscordId(discordId).blockOptional();
        return playerDtoOptional.flatMap(playerDto -> paradisePlayerRepository.findByCkey(playerDto.getCkey())
                .map(paradisePlayer -> mappers.toPlayer(playerDto, paradisePlayer)));
    }
}
