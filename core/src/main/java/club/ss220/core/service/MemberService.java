package club.ss220.core.service;

import club.ss220.core.data.integration.central.CentralApiClient;
import club.ss220.core.data.integration.central.UserDto;
import club.ss220.core.data.integration.game.PlayerRepositoryAdapter;
import club.ss220.core.data.mapper.Mappers;
import club.ss220.core.model.Member;
import club.ss220.core.model.Player;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService {

    private final CentralApiClient centralApiClient;
    private final List<PlayerRepositoryAdapter> playerRepositories;
    private final Mappers mappers;

    public Optional<Member> getMemberByCkey(String ckey) {
        return centralApiClient.getMemberByCkey(ckey).blockOptional().map(this::toMember);
    }

    public Optional<Member> getMemberByDiscordId(Long discordId) {
        return centralApiClient.getMemberByDiscordId(discordId).blockOptional().map(this::toMember);
    }

    private Member toMember(UserDto userDto) {
        List<Player> players = playerRepositories.stream()
                .map(repository -> repository.findByCkey(userDto.getCkey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return mappers.toMember(userDto, players);
    }
}
