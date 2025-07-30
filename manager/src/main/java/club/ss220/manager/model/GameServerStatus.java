package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameServerStatus {
    private final String version;
    private final Integer playersCount;
    private final String stationTime;
    private final String roundDuration;
    private final String map;
    private final Integer adminsCount;
    private final Integer roundId;
    private final String mode;
}
