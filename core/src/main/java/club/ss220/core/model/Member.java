package club.ss220.core.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.TreeMap;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Member extends User {

    @NotNull
    private final TreeMap<GameBuild, Player> gameInfo;

    @Builder
    public Member(@NotNull Integer id, @NotNull String ckey, @NotNull Long discordId,
                  @NotNull TreeMap<GameBuild, Player> gameInfo) {
        super(id, ckey, discordId);
        this.gameInfo = gameInfo;
    }
}
