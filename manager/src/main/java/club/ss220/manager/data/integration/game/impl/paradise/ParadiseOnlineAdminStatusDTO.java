package club.ss220.manager.data.integration.game.impl.paradise;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.OnlineAdminStatus;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class ParadiseOnlineAdminStatusDTO extends ServerResponse implements OnlineAdminStatus {

    @Override
    public String getCkey() {
        String property = "ckey";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .orElseThrow(() -> propertyNotFound(property));
    }

    @Override
    public String getKey() {
        String property = "key";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .orElseThrow(() -> propertyNotFound(property));
    }

    @Override
    public List<String> getRanks() {
        String property = "rank";
        String rank = Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .orElseThrow(() -> propertyNotFound(property));
        return List.of(rank);
    }

    @Override
    public Duration getAfkDuration() {
        String property = "afk";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .map(deciseconds -> Duration.ofMillis(deciseconds * 100))
                .orElseThrow(() -> propertyNotFound(property));
    }

    @Override
    public StealthMode getStealthMode() {
        String property = "stealth_mode";
        String value = Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .orElseThrow(() -> propertyNotFound(property));

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
        if (getStealthMode() == StealthMode.NONE) {
            return null;
        }

        String property = "skey";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .orElseThrow(() -> propertyNotFound(property));
    }
}
