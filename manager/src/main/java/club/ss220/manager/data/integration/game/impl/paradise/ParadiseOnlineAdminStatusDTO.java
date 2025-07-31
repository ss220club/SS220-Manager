package club.ss220.manager.data.integration.game.impl.paradise;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.OnlineAdminStatus;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class ParadiseOnlineAdminStatusDTO extends ServerResponse implements OnlineAdminStatus {

    @Override
    public String getCkey() {
        return Optional.ofNullable(data.get("ckey"))
                .map(String::valueOf)
                .orElseThrow();
    }

    @Override
    public String getKey() {
        return Optional.ofNullable(data.get("key"))
                .map(String::valueOf)
                .orElseThrow();
    }

    @Override
    public List<String> getRanks() {
        String rank = Optional.ofNullable(data.get("rank"))
                .map(String::valueOf)
                .orElseThrow();
        return List.of(rank);
    }

    @Override
    public Duration getAfkDuration() {
        return Optional.ofNullable(data.get("afk"))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .map(deciseconds -> Duration.ofMillis(deciseconds * 100))
                .orElseThrow();
    }

    @Override
    public StealthMode getStealthMode() {
        String value = Optional.ofNullable(data.get("stealth_mode"))
                .map(String::valueOf)
                .orElseThrow();
        String valueLower = value.toLowerCase();
        if (valueLower.contains("stealth")) {
            return StealthMode.STEALTH;
        } else if (valueLower.contains("bb")) {
            return StealthMode.BIG_BROTHER;
        } else {
            return StealthMode.NONE;
        }
    }

    @Override
    public String getStealthKey() {
        if (getStealthMode() == StealthMode.STEALTH) {
            return null;
        }
        return Optional.ofNullable(data.get("skey"))
                .map(String::valueOf)
                .orElseThrow();
    }
}
