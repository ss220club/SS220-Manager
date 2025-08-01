package club.ss220.manager.data.db.game.paradise.repository;

import club.ss220.manager.config.GameConfig;
import club.ss220.manager.data.db.game.CharacterRepositoryAdapter;
import club.ss220.manager.data.mapper.Mappers;
import club.ss220.manager.model.GameCharacter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier(GameConfig.BUILD_PARADISE)
public class ParadiseCharacterRepositoryAdapter implements CharacterRepositoryAdapter {

    private final ParadiseCharacterRepository repository;
    private final Mappers mappers;

    public ParadiseCharacterRepositoryAdapter(ParadiseCharacterRepository repository, Mappers mappers) {
        this.repository = repository;
        this.mappers = mappers;
    }

    @Override
    public List<GameCharacter> findByCkey(String ckey) {
        return repository.findByCkey(ckey).stream().map(mappers::toGameCharacter).toList();
    }

    @Override
    public List<GameCharacter> findByName(String name) {
        return repository.findByRealNameContainingIgnoreCase(name).stream().map(mappers::toGameCharacter).toList();
    }
}
