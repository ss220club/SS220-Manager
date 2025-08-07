package club.ss220.manager.service;

import club.ss220.manager.data.db.game.CharacterRepositoryAdapter;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.util.ByondUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GameCharacterService {

    private final GameBuildStrategyService strategyService;
    private final ByondUtils byondUtils;

    public List<GameCharacter> getCharactersByCkey(GameBuild gameBuild, String ckey) {
        String sanitizedCkey = byondUtils.sanitizeCkey(ckey);
        CharacterRepositoryAdapter repository = strategyService.getCharacterRepository(gameBuild);
        return repository.findByCkey(sanitizedCkey);
    }

    public List<GameCharacter> getCharactersByName(GameBuild gameBuild, String name) {
        CharacterRepositoryAdapter repository = strategyService.getCharacterRepository(gameBuild);
        return repository.findByName(name);
    }
}
