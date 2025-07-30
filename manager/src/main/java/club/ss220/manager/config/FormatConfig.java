package club.ss220.manager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("application.format")
public class FormatConfig {

    private String dateTimeFormat = "dd.MM.yyyy HH:mm";
    private String durationFormat = "${days}d ${hours}h ${minutes}m ${seconds}s";
}
