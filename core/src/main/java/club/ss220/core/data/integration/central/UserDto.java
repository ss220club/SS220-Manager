package club.ss220.core.data.integration.central;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @JsonProperty("id")
    private int id;
    @JsonProperty("ckey")
    private String ckey;
    @JsonProperty("discord_id")
    private long discordId;
}
