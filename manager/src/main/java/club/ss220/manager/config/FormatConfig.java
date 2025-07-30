package club.ss220.manager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("application.format")
public class FormatConfig {

    private String dateTimeFormat = "dd.MM.yyyy HH:mm";
    private String durationFormat = "%1$dd %2$dh %3$dm %4$ds";
    private String roundDurationFormat = "%2$02d:%3$02d:%4$02d";
}
