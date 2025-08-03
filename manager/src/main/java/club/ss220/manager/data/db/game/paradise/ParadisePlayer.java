package club.ss220.manager.data.db.game.paradise;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "player")
@NoArgsConstructor
@AllArgsConstructor
public class ParadisePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "ckey")
    private String ckey;

    @Column(name = "byond_date")
    private LocalDate byondJoinDate;

    @Column(name = "firstseen")
    private LocalDateTime firstSeen;

    @Column(name = "lastseen")
    private LocalDateTime lastSeen;

    @Column(name = "ip")
    private String ip;

    @Column(name = "computerid")
    private String computerId;

    @Column(name = "lastadminrank")
    private String lastAdminRank;

    @Column(name = "exp")
    private String exp;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "ckey", referencedColumnName = "ckey")
    private List<ParadiseCharacter> characters;
}
