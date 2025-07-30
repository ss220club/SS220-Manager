package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameServer {
    private final String name;
    private final String build;
    private final String host;
    private final int port;
    private final String key;
}
