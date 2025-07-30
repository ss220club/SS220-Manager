package club.ss220.manager.app.util;

import club.ss220.manager.config.FormatConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        return formatConfig.getDurationFormat().formatted(
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart(),
                duration.toSecondsPart()
        );
    }

    public String formatRoundDuration(Duration duration) {
        return formatConfig.getRoundDurationFormat().formatted(
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart(),
                duration.toSecondsPart()
        );
    }
}
