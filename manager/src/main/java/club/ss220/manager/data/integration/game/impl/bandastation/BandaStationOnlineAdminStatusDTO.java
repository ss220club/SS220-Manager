package club.ss220.manager.data.integration.game.impl.bandastation;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.OnlineAdminStatus;
import org.apache.commons.lang3.stream.Streams;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BandaStationOnlineAdminStatusDTO extends ServerResponse implements OnlineAdminStatus {

    @Override
    public String getCkey() {
        return Optional.ofNullable(data.get("ckey"))
                .map(String::valueOf)
                .orElseThrow(() -> throwInvalidData(data));
    }

    @Override
    public String getKey() {
        return Optional.ofNullable(data.get("key"))
                .map(String::valueOf)
                .orElseThrow(() -> throwInvalidData(data));
    }

    @Override
    public List<String> getRanks() {
        Object rawRanks = Optional.ofNullable(data.get("rank")).orElseThrow(() -> throwInvalidData(data));
        if (rawRanks instanceof Iterable<?>) {
            return Streams.of(rawRanks).map(String::valueOf).toList();
        }
        return List.of(String.valueOf(rawRanks));
    }

    @Override
    public Duration getAfkDuration() {
        return Optional.ofNullable(data.get("afk"))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .map(deciseconds -> Duration.ofMillis(deciseconds * 100))
                .orElseThrow(() -> throwInvalidData(data));
    }

    @Override
    public StealthMode getStealthMode() {
        String value = Optional.ofNullable(data.get("stealth_mode"))
                .map(String::valueOf)
                .orElseThrow(() -> throwInvalidData(data));
        return value.toLowerCase().contains("stealth") ? StealthMode.STEALTH : StealthMode.NONE;
    }

    @Override
    public String getStealthKey() {
        if (getStealthMode() == StealthMode.STEALTH) {
            return null;
        }
        return Optional.ofNullable(data.get("skey"))
                .map(String::valueOf)
                .orElseThrow(() -> throwInvalidData(data));
    }

    private static RuntimeException throwInvalidData(Map<String, Object> data) {
        return new IllegalArgumentException("Invalid data: " + data);
    }
}
