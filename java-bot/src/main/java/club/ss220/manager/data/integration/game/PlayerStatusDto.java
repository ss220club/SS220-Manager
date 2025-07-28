package club.ss220.manager.data.integration.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PlayerStatusDto {
    @JsonProperty("ckey")
    private String ckey;

    @JsonProperty("character")
    private String characterName;

    @JsonProperty("job")
    private String job;

    @JsonProperty("rank")
    private String rank;
}
