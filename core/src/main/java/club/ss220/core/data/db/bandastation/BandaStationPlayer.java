package club.ss220.core.data.db.bandastation;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "player")
public class BandaStationPlayer {

    @Id
    @Column(name = "ckey")
    private String ckey;

    @Column(name = "accountjoindate")
    private LocalDate byondJoinDate;

    @Column(name = "firstseen")
    private LocalDateTime firstSeen;

    @Column(name = "lastseen")
    private LocalDateTime lastSeen;

    @Column(name = "ip", columnDefinition = "INT UNSIGNED")
    private long ip;

    @Column(name = "computerid")
    private String computerId;

    @Column(name = "lastadminrank")
    private String lastAdminRank;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_time", joinColumns = @JoinColumn(name = "ckey"))
    @MapKeyColumn(name = "job")
    @Column(name = "minutes", columnDefinition = "INT UNSIGNED")
    private Map<String, Long> roleTime;
}
