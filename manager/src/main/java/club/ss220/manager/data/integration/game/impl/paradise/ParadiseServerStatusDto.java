package club.ss220.manager.data.integration.game.impl.paradise;

import club.ss220.manager.data.integration.game.impl.ServerResponse;
import club.ss220.manager.model.GameServerStatus;

import java.time.Duration;
import java.util.Optional;

public class ParadiseServerStatusDto extends ServerResponse implements GameServerStatus {

    @Override
    public Integer getPlayers() {
        return Optional.ofNullable(data.get("players"))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .orElseThrow();
    }

    @Override
    public Integer getAdmins() {
        return Optional.ofNullable(data.get("admins"))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .orElseThrow();
    }

    @Override
    public Duration getRoundDuration() {
        return Optional.ofNullable(data.get("roundtime"))
                .map(String::valueOf)
                .map(ParadiseServerStatusDto::parseDuration)
                .orElseThrow();
    }

    private static Duration parseDuration(String duration) {
        String[] parts = duration.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
