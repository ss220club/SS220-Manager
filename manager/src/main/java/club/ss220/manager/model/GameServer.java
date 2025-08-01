package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameServer {
    private final String name;
    private final GameBuild build;
    private final String host;
    private final int port;
    private final String key;

    public String getFullName() {
        return build.getName() + " " + name;
    }
}
