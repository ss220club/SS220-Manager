package club.ss220.manager.data.db.game.bandastation;

import club.ss220.manager.data.db.game.bandastation.converter.UnsignedIntConverter;
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

    @Column(name = "ip")
    @Convert(converter = UnsignedIntConverter.class)
    private long ip;

    @Column(name = "computerid")
    private String computerId;

    @Column(name = "lastadminrank")
    private String lastAdminRank;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_time", joinColumns = @JoinColumn(name = "ckey"))
    @MapKeyColumn(name = "job")
    @Column(name = "minutes")
    @Convert(attributeName = "value", converter = UnsignedIntConverter.class)
    private Map<String, Long> roleTime;
}
