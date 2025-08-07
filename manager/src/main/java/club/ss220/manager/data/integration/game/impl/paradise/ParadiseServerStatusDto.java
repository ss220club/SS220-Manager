package club.ss220.manager.data.integration.game.impl.paradise;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.GameServerStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ParadiseServerStatusDto extends ServerResponse implements GameServerStatus {

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
        String property = "roundtime";
        return Optional.ofNullable(data.get(property))
                .map(String::valueOf)
                .map(ParadiseServerStatusDto::parseDuration)
                .orElseThrow(() -> propertyNotFound(property));
    }

    @NotNull
    @Override
    public Map<String, Object> getRawData() {
        return Collections.unmodifiableMap(data);
    }

    private static Duration parseDuration(String duration) {
        String[] parts = duration.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
