package club.ss220.manager.model;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

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
