package club.ss220.manager.config;

import club.ss220.manager.data.db.game.CharacterRepositoryAdapter;
import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.model.GameBuild;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class GameBuildStrategyConfig {

    @Bean
    public Map<GameBuild, GameApiClient> gameApiClientMap(
            @Qualifier(GameConfig.BUILD_PARADISE) GameApiClient paradiseClient,
            @Qualifier(GameConfig.BUILD_BANDASTRATION) GameApiClient bandastationClient) {
        return Map.of(
                GameBuild.PARADISE, paradiseClient,
                GameBuild.BANDASTATION, bandastationClient
        );
    }

    @Bean
    public Map<GameBuild, CharacterRepositoryAdapter> characterRepositoryMap(
            @Qualifier(GameConfig.BUILD_PARADISE) CharacterRepositoryAdapter paradiseRepository) {
        return Map.of(
                GameBuild.PARADISE, paradiseRepository
                // TODO: 01.08.2025 Provide repository here when bandastation will store game characters in a database.
        );
    }
}
