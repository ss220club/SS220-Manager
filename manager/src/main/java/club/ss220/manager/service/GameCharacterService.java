package club.ss220.manager.service;

import club.ss220.manager.data.db.paradise.repository.ParadiseCharacterRepository;
import club.ss220.manager.data.mapper.Mappers;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.util.CkeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GameCharacterService {

    private final ParadiseCharacterRepository paradiseCharacterRepository;
    private final Mappers mappers;

    public GameCharacterService(ParadiseCharacterRepository paradiseCharacterRepository, Mappers mappers) {
        this.paradiseCharacterRepository = paradiseCharacterRepository;
        this.mappers = mappers;
    }

    public List<GameCharacter> getCharactersByCkey(String ckey) {
        String sanitizedCkey = CkeyUtils.sanitizeCkey(ckey);
        return paradiseCharacterRepository.findByCkey(sanitizedCkey)
                .stream()
                .map(mappers::toGameCharacter)
                .toList();
    }

    public List<GameCharacter> getCharactersByName(String name) {
        return paradiseCharacterRepository.findByRealNameContainingIgnoreCase(name)
                .stream()
                .map(mappers::toGameCharacter)
                .toList();
    }
}
