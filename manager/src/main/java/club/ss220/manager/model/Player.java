package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Player {

    @NotNull
    private final GameBuild gameBuild;
    @NotNull
    private final String ckey;
    @NotNull
    private final LocalDate byondJoinDate;
    @NotNull
    private final LocalDateTime firstSeenDateTime;
    @NotNull
    private final LocalDateTime lastSeenDateTime;
    @NotNull
    @ToString.Exclude
    private final InetAddress ip;
    @NotNull
    @ToString.Exclude
    private final String computerId;
    @NotNull
    private final String lastAdminRank;
    @NotNull
    private final PlayerExperience exp;
    @Nullable // TODO: Update this when bandastation will store game characters in a database.
    private final List<GameCharacter> characters;

    public Duration getKnownFor() {
        return Duration.between(firstSeenDateTime, lastSeenDateTime);
    }
}
