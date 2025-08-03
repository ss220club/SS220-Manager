package club.ss220.manager.data.db.game;

import club.ss220.manager.model.Player;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepositoryAdapter {

    Optional<Player> findByCkey(String ckey);
}
