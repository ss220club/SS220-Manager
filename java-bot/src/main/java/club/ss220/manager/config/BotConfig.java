package club.ss220.manager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String token;
    private String dateTimeFormat = "dd.MM.yyyy HH:mm";

    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern(dateTimeFormat);
    }
}
