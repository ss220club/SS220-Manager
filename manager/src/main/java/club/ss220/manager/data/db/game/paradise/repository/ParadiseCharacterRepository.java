package club.ss220.manager.data.db.game.paradise.repository;

import club.ss220.manager.data.db.game.paradise.ParadiseCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadiseCharacterRepository extends JpaRepository<ParadiseCharacter, Integer> {

    List<ParadiseCharacter> findByCkey(String ckey);

    List<ParadiseCharacter> findByRealNameContainingIgnoreCase(String name);
}
