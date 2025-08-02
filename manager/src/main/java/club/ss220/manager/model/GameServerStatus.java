package club.ss220.manager.model;

import java.time.Duration;
import java.util.Map;

public interface GameServerStatus {

    Integer getPlayers();

    Integer getAdmins();

    Duration getRoundDuration();

    Map<String, Object> getRawData();
}
