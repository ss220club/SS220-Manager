package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnlinePlayer {
    private final String ckey;
    private final String characterName;
    private final String job;
}
