package club.ss220.manager.data.db.game.paradise.repository;

import club.ss220.manager.data.db.game.paradise.ParadisePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParadisePlayerRepository extends JpaRepository<ParadisePlayer, Integer> {
    
    Optional<ParadisePlayer> findByCkey(String ckey);
}
