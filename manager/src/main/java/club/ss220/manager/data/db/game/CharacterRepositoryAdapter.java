package club.ss220.manager.data.db.game;

import club.ss220.manager.model.GameCharacter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepositoryAdapter {

    List<GameCharacter> findByCkey(String ckey);

    List<GameCharacter> findByName(String name);
}
