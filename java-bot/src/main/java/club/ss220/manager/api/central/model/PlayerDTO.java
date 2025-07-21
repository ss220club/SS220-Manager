package club.ss220.manager.api.central.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private int id;
    private String ckey;
    private long discordId;
}
