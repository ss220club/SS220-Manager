package club.ss220.manager.app.util;

import club.ss220.manager.config.FormatConfig;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class Formatters {

    private final FormatConfig formatConfig;

    public Formatters(FormatConfig formatConfig) {
        this.formatConfig = formatConfig;
    }

    public String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(formatConfig.getDateTimeFormat()));
    }

    public String formatDuration(Duration duration) {
        return StringSubstitutor.replace(formatConfig.getDurationFormat(), Map.of(
                "days", duration.toDaysPart(),
                "hours", duration.toHoursPart(),
                "minutes", duration.toMinutesPart(),
                "seconds", duration.toSecondsPart()));
    }
}
