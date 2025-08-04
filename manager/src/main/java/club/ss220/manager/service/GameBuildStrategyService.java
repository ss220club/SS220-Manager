package club.ss220.manager.service;

import club.ss220.manager.data.db.game.CharacterRepositoryAdapter;
import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.model.GameBuild;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class GameBuildStrategyService {

    private final Map<GameBuild, GameApiClient> gameApiClients;
    private final Map<GameBuild, CharacterRepositoryAdapter> characterRepositories;

    public GameApiClient getGameApiClient(GameBuild gameBuild) {
        return Optional.ofNullable(gameApiClients.get(gameBuild))
                .orElseThrow(() -> throwError("Game API client not available for build: " + gameBuild.getName()));
    }

    public CharacterRepositoryAdapter getCharacterRepository(GameBuild gameBuild) {
        return Optional.ofNullable(characterRepositories.get(gameBuild))
                .orElseThrow(() -> throwError("Character repository not available for build: " + gameBuild.getName()));
    }

    private RuntimeException throwError(String message) {
        return new UnsupportedOperationException(message);
    }
}
