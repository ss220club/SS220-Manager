package club.ss220.manager.data.integration.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServerStatusDto {
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("players")
    private Integer playersCount;
    
    @JsonProperty("stationtime")
    private String stationTime;
    
    @JsonProperty("roundtime")
    private String roundDuration;

    @JsonProperty("map")
    private String map;
    
    @JsonProperty("admins")
    private Integer adminsCount;
    
    @JsonProperty("round_id")
    private Integer roundId;

    @JsonProperty("mode")
    private String mode;

    public static ServerStatusDto unknown() {
        ServerStatusDto unknownStatus = new ServerStatusDto();
        unknownStatus.setVersion("N/A");
        unknownStatus.setPlayersCount(-1);
        unknownStatus.setStationTime("N/A");
        unknownStatus.setRoundDuration("N/A");
        unknownStatus.setMap("N/A");
        unknownStatus.setAdminsCount(-1);
        unknownStatus.setRoundId(-1);
        unknownStatus.setMode("N/A");
        return unknownStatus;
    }
}
