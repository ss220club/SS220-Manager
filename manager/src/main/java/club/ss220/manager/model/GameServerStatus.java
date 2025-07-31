package club.ss220.manager.model;

import java.time.Duration;

public interface GameServerStatus {

    Integer getPlayers();

    Integer getAdmins();

    Duration getRoundDuration();
}
