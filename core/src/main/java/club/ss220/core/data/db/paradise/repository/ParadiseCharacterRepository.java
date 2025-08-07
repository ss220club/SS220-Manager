package club.ss220.core.data.db.paradise.repository;

import club.ss220.core.data.db.paradise.ParadiseCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadiseCharacterRepository extends JpaRepository<ParadiseCharacter, Integer> {

    List<ParadiseCharacter> findByCkey(String ckey);

    List<ParadiseCharacter> findByRealNameContainingIgnoreCase(String name);
}
