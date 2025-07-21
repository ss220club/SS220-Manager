package club.ss220.manager.db.paradise.repository;

import club.ss220.manager.db.paradise.entity.GameCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameCharacterRepository extends JpaRepository<GameCharacter, Integer> {
    
    List<GameCharacter> findByCkey(String ckey);
    
    List<GameCharacter> findByRealNameContainingIgnoreCase(String name);
}
