package club.ss220.manager.data.db.game.paradise.repository;

import club.ss220.manager.data.db.game.paradise.ParadiseBan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParadiseBanRepository extends JpaRepository<ParadiseBan, Integer> {

    @Query("SELECT b FROM ParadiseBan b WHERE b.ckey = :ckey AND b.banDatetime <= :dateTime ORDER BY b.banDatetime DESC")
    List<ParadiseBan> findRecentPlayerBans(String ckey, LocalDateTime banDatetime, Pageable pageable);

    @Query("SELECT b FROM ParadiseBan b WHERE b.ckey = :ckey ORDER BY b.banDatetime DESC")
    List<ParadiseBan> findPlayerBans(String ckey, Pageable pageable);

    Integer countBansByCkey(String ckey);
}
