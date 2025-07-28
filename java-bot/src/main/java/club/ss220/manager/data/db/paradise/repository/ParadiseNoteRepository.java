package club.ss220.manager.data.db.paradise.repository;

import club.ss220.manager.data.db.paradise.ParadiseNote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParadiseNoteRepository extends JpaRepository<ParadiseNote, Long> {

    @Query("SELECT n FROM ParadiseNote n WHERE n.ckey = :ckey ORDER BY n.timestamp DESC")
    List<ParadiseNote> findByCkeyOrderByTimestampDesc(@Param("ckey") String ckey, Pageable pageable);

    @Query("SELECT n FROM ParadiseNote n WHERE n.ckey = :ckey ORDER BY n.timestamp DESC")
    List<ParadiseNote> findPublicNotesByCkeyOrderByTimestampDesc(@Param("ckey") String ckey, Pageable pageable);
}
