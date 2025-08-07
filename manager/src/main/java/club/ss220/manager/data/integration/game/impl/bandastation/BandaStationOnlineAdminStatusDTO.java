package club.ss220.manager.data.integration.game.impl.bandastation;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.OnlineAdminStatus;
import org.apache.commons.lang3.stream.Streams;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class BandaStationOnlineAdminStatusDTO extends ServerResponse implements OnlineAdminStatus {

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
        Object rawRanks = Optional.ofNullable(data.get(property)).orElseThrow(() -> propertyNotFound(property));
        if (rawRanks instanceof Iterable<?>) {
            return Streams.of(rawRanks).map(String::valueOf).toList();
        }
        return List.of(String.valueOf(rawRanks));
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
        return value.toLowerCase().contains("stealth") ? StealthMode.STEALTH : StealthMode.NONE;
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
