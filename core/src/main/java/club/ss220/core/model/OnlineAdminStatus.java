package club.ss220.core.model;

import jakarta.annotation.Nullable;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

public interface OnlineAdminStatus {

    String getCkey();

    String getKey();

    List<String> getRanks();

    Duration getAfkDuration();

    StealthMode getStealthMode();

    @Nullable
    String getStealthKey();

    @Getter
    enum StealthMode {
        NONE,
        STEALTH,
        BIG_BROTHER
    }
}
