package club.ss220.core.config;

import club.ss220.core.model.GameServer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Data
@Configuration
@ConfigurationProperties("application.integration.game")
public class GameConfig {

    public static final String BUILD_PARADISE = "paradise";
    public static final String BUILD_BANDASTRATION = "bandastation";

    private List<GameServer> servers;

    public Optional<GameServer> getServerByName(String serverName) {
        return servers.stream()
                .filter(server -> server.getName().equalsIgnoreCase(serverName))
                .findFirst();
    }
}
