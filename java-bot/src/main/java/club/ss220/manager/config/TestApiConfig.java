package club.ss220.manager.config;

import club.ss220.manager.api.central.CentralApiClient;
import club.ss220.manager.api.central.model.PlayerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@Profile("test")
public class TestApiConfig {

    @Value("${api.central.endpoint}")
    private String apiEndpoint;

    @Value("${api.central.token}")
    private String apiToken;

    @Bean
    @Primary
    public CentralApiClient testCentralApiClient() {
        return new TestCentralApiClient(apiEndpoint, apiToken);
    }

    public static class TestCentralApiClient extends CentralApiClient {

        private final List<PlayerDTO> players = List.of(
                new PlayerDTO(1, "testckey1", 1L),
                new PlayerDTO(2, "testckey2", 2L)
        );

        public TestCentralApiClient(String endpoint, String token) {
            super(endpoint, token);
        }

        @Override
        public Mono<PlayerDTO> getPlayerByCkey(String ckey) {
            return Mono.justOrEmpty(players.stream()
                    .filter(p -> p.getCkey().equals(ckey))
                    .findFirst());
        }

        @Override
        public Mono<PlayerDTO> getPlayerByDiscordId(long discordId) {
            return Mono.justOrEmpty(players.stream()
                    .filter(p -> p.getDiscordId() == discordId)
                    .findFirst());
        }
    }
}
