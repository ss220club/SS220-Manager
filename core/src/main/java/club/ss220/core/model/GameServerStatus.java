package club.ss220.core.model;

import jakarta.validation.constraints.NotNull;

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
