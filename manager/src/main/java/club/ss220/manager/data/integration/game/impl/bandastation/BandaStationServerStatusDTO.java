package club.ss220.manager.data.integration.game.impl.bandastation;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.GameServerStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class BandaStationServerStatusDTO extends ServerResponse implements GameServerStatus {

    @NotNull
    @Override
    public Integer getPlayers() {
        String property = "players";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .orElseThrow(() -> propertyNotFound(property));
    }

    @NotNull
    @Override
    public Integer getAdmins() {
        String property = "admins";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .orElseThrow(() -> propertyNotFound(property));
    }

    @NotNull
    @Override
    public Duration getRoundDuration() {
        String property = "round_duration";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .map(Duration::ofSeconds)
                .orElseThrow(() -> propertyNotFound(property));
    }

    @NotNull
    @Override
    public Map<String, Object> getRawData() {
        return Collections.unmodifiableMap(data);
    }
}
