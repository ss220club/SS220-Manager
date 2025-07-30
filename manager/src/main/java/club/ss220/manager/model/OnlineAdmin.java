package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Duration;
import java.util.Arrays;
import java.util.NoSuchElementException;

@Data
@Builder
public class OnlineAdmin {
    private final String ckey;
    private final String key;
    private final String rank;
    private final Duration afkDuration;
    private final StealthMode stealthMode;
    private final String stealthKey;

    @Getter
    public enum StealthMode {
        NONE("NONE"),
        STEALTH("STEALTH"),
        BIG_BROTHER("BB");

        private final String value;

        StealthMode(String value) {
            this.value = value;
        }

        public static StealthMode fromValue(String value) {
            return Arrays.stream(StealthMode.values())
                    .filter(mode -> mode.value.equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Unknown stealth mode: " + value));
        }
    }
}
