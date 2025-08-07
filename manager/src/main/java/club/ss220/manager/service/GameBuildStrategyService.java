package club.ss220.manager.service;

import club.ss220.manager.data.db.game.CharacterRepositoryAdapter;
import club.ss220.manager.data.integration.game.GameApiClient;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.exception.GameBuildOperationNotSupportedException;
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
                .orElseThrow(() -> operationNotSupported(gameBuild, "game API client"));
    }

    public CharacterRepositoryAdapter getCharacterRepository(GameBuild gameBuild) {
        return Optional.ofNullable(characterRepositories.get(gameBuild))
                .orElseThrow(() -> operationNotSupported(gameBuild, "character repository"));
    }

    private RuntimeException operationNotSupported(GameBuild gameBuild, String operation) {
        String message = operation + " is not available for build: " + gameBuild.getName();
        return new GameBuildOperationNotSupportedException(gameBuild, message);
    }
}
