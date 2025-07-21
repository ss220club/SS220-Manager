package club.ss220.manager.service;

import club.ss220.manager.db.paradise.entity.GameCharacter;
import club.ss220.manager.db.paradise.repository.GameCharacterRepository;
import club.ss220.manager.util.CkeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GameCharacterService {

    private final GameCharacterRepository gameCharacterRepository;

    public GameCharacterService(GameCharacterRepository gameCharacterRepository) {
        this.gameCharacterRepository = gameCharacterRepository;
    }

    public List<GameCharacter> getCharactersByCkey(String ckey) {
        String sanitizedCkey = CkeyUtils.sanitizeCkey(ckey);
        return gameCharacterRepository.findByCkey(sanitizedCkey);
    }

    public List<GameCharacter> getCharactersByName(String name) {
        return gameCharacterRepository.findByRealNameContainingIgnoreCase(name);
    }
}
