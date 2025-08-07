package club.ss220.core.data.db.paradise;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "ban")
@Data
public class ParadiseBan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "ckey", nullable = false)
    private String ckey;
    
    @Column(name = "a_ckey")
    private String adminCkey;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "bantime")
    private LocalDateTime banDatetime;
    
    @Column(name = "unbanned_datetime")
    private LocalDateTime unbanDatetime;
    
    @Column(name = "bantype")
    private String banType;
    
    @Column(name = "unbanned")
    private Boolean unbanned;
    
    @Column(name = "expiration_time")
    private LocalDateTime expirationDatetime;
    
    public boolean isActive() {
        if (unbanned != null && unbanned) {
            return false;
        }
        if (expirationDatetime != null && expirationDatetime.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            return false;
        }
        return true;
    }
}
