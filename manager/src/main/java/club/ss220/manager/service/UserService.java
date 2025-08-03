package club.ss220.manager.service;

import club.ss220.manager.data.api.impl.CentralApiClientImpl;
import club.ss220.manager.data.api.UserDto;
import club.ss220.manager.data.db.game.PlayerRepositoryAdapter;
import club.ss220.manager.data.mapper.Mappers;
import club.ss220.manager.model.Player;
import club.ss220.manager.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final CentralApiClientImpl centralApiClient;
    private final List<PlayerRepositoryAdapter> playerRepositories;
    private final Mappers mappers;

    public Optional<User> getUserByCkey(String ckey) {
        return centralApiClient.getUserByCkey(ckey).blockOptional().map(this::toUser);
    }

    public Optional<User> getUserByDiscordId(Long discordId) {
        return centralApiClient.getUserByDiscordId(discordId).blockOptional().map(this::toUser);
    }

    private User toUser(UserDto userDto) {
        List<Player> players = playerRepositories.stream()
                .map(repository -> repository.findByCkey(userDto.getCkey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return mappers.toUser(userDto, players);
    }
}
