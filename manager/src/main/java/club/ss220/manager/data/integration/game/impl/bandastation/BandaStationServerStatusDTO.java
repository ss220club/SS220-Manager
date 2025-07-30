package club.ss220.manager.data.integration.game.impl.bandastation;

import club.ss220.manager.model.GameServerStatus;

import java.time.Duration;
import java.util.Optional;

public class BandaStationServerStatusDTO extends GameServerStatus {

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
        return Optional.ofNullable(data.get("round_duration"))
                .map(String::valueOf)
                .map(Integer::parseInt)
                .map(Duration::ofSeconds)
                .orElseThrow();
    }
}
