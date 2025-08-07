package club.ss220.core.data.db.bandastation.repository;

import club.ss220.core.data.db.bandastation.BandaStationPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BandaStationPlayerRepository extends JpaRepository<BandaStationPlayer, Long> {

    Optional<BandaStationPlayer> findByCkey(String ckey);
}
