package club.ss220.manager.data.integration.game.impl.paradise;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminStatusDto {

    @JsonProperty("ckey")
    private final String ckey;

    @JsonProperty("key")
    private final String key;

    @JsonProperty("rank")
    private final String rank;

    @JsonProperty("afk")
    private final Integer afkDuration; // in deciseconds

    @JsonProperty("stealth")
    private final String stealthMode;

    @JsonProperty("skey")
    private final String stealthKey;
}
