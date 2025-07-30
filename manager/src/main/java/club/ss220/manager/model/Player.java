package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Player {

    private final Integer id;
    private final Long discordId;
    private final String ckey;
    private final LocalDateTime firstSeen;
    private final LocalDateTime lastSeen;
    private final String ip;
    private final String computerId;
    private final String lastAdminRank;
    private final String exp;
    private final String speciesWhitelist;
}
