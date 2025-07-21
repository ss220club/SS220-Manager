package club.ss220.manager.db.paradise.repository;

import club.ss220.manager.db.paradise.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    
    Optional<Player> findByCkey(String ckey);
}
