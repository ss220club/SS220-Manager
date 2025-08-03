package club.ss220.manager.data.db.game.bandastation.repository;

import club.ss220.manager.config.GameConfig;
import club.ss220.manager.data.db.game.PlayerRepositoryAdapter;
import club.ss220.manager.data.mapper.Mappers;
import club.ss220.manager.model.Player;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Qualifier(GameConfig.BUILD_BANDASTRATION)
public class BandaStationPlayerRepositoryAdapter implements PlayerRepositoryAdapter {

    private final BandaStationPlayerRepository repository;
    private final Mappers mappers;

    public BandaStationPlayerRepositoryAdapter(BandaStationPlayerRepository repository, Mappers mappers) {
        this.repository = repository;
        this.mappers = mappers;
    }

    @Override
    public Optional<Player> findByCkey(String ckey) {
        return repository.findByCkey(ckey).map(mappers::toPlayer);
    }
}
