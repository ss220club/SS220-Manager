package club.ss220.manager.config;

import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.data.integration.game.impl.bandastation.BandaStationApiClientImpl;
import club.ss220.manager.data.integration.game.impl.paradise.ParadiseApiClientImpl;
import club.ss220.manager.model.GameServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class GameApiClientConfig {

    @Bean
    public GameApiClient paradiseClient() {
        return new ParadiseApiClientImpl();
    }

    @Bean
    public GameApiClient bandaStationClient() {
        return new BandaStationApiClientImpl();
    }

    @Bean
    public Map<GameServer.Build, GameApiClient> gameApiClients(GameApiClient paradiseClient,
                                                               GameApiClient bandaStationClient) {
        return Stream.of(paradiseClient, bandaStationClient)
                .collect(Collectors.toMap(
                        GameApiClient::getBuild,
                        Function.identity(),
                        (a, _) -> { throw onClientConflict(a.getBuild()); }
                ));
    }

    private static RuntimeException onClientConflict(GameServer.Build build) {
        return new IllegalStateException("Multiple game API clients found for build " + build);
    }
}
