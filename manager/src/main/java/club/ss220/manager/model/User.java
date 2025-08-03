package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
public class User {
    
    @NotNull
    private final Integer id;
    @NotNull
    private final String ckey;
    @NotNull
    private final Long discordId;
    @NotNull
    private final TreeMap<GameBuild, Player> gameInfo;

    public User(@NotNull Integer id, @NotNull String ckey, @NotNull Long discordId,
                @NotNull TreeMap<GameBuild, Player> gameInfo) {
        this.id = id;
        this.ckey = ckey;
        this.discordId = discordId;
        this.gameInfo = gameInfo;
        validate();
    }

    private void validate() {
        Stream<String> gameCkeys = gameInfo.values().stream().map(Player::getCkey);
        Set<String> ckeys = Stream.concat(Stream.of(ckey), gameCkeys).collect(Collectors.toSet());
        if (ckeys.size() != 1) {
            throw new IllegalStateException("User has multiple ckeys: " + ckeys);
        }
    }
}
