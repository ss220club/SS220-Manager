package club.ss220.core.data.integration.game;

import club.ss220.core.model.Player;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepositoryAdapter {

    Optional<Player> findByCkey(String ckey);
}
