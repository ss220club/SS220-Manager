package club.ss220.manager.model;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;

public interface GameServerStatus {

    @NotNull
    Integer getPlayers();

    @NotNull
    Integer getAdmins();

    @NotNull
    Duration getRoundDuration();

    @NotNull
    Map<String, Object> getRawData();
}
