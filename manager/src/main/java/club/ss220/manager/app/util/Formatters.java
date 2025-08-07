package club.ss220.manager.app.util;

import club.ss220.core.config.FormatConfig;
import com.ibm.icu.text.MessageFormat;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@AllArgsConstructor
public class Formatters {

    public static final Locale LOCALE = Locale.of("ru");

    private final FormatConfig formatConfig;

    public String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(formatConfig.getDateFormat()));
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

    public String formatPlural(String pattern, Object... args) {
        MessageFormat messageFormat = new MessageFormat(pattern, LOCALE);
        return messageFormat.format(args);
    }
}
