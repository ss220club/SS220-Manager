package club.ss220.core.data.db.paradise.repository;

import club.ss220.core.config.GameConfig;
import club.ss220.core.data.integration.game.PlayerRepositoryAdapter;
import club.ss220.core.data.mapper.Mappers;
import club.ss220.core.model.Player;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Qualifier(GameConfig.BUILD_PARADISE)
public class ParadisePlayerRepositoryAdapter implements PlayerRepositoryAdapter {

    private final ParadisePlayerRepository repository;
    private final Mappers mappers;

    public ParadisePlayerRepositoryAdapter(ParadisePlayerRepository repository, Mappers mappers) {
        this.repository = repository;
        this.mappers = mappers;
    }

    @Override
    public Optional<Player> findByCkey(String ckey) {
        return repository.findByCkey(ckey).map(mappers::toPlayer);
    }
}
