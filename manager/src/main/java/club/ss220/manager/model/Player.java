package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

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
    private final InetAddress ip;
    @NotNull
    private final String computerId;
    @NotNull
    private final String lastAdminRank;
    @NotNull
    private final TreeMap<RoleCategory, Duration> exp;
    @Nullable // TODO: Update this when bandastation will store game characters in a database.
    private final List<GameCharacter> characters;

    public Duration getKnownFor() {
        return Duration.between(firstSeenDateTime, lastSeenDateTime);
    }
}
