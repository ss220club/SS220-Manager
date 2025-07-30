package club.ss220.manager.data.integration.game.impl.bandastation;

import club.ss220.manager.data.integration.game.impl.AbstractGameApiClient;
import club.ss220.manager.data.integration.game.impl.paradise.AdminStatusDto;
import club.ss220.manager.model.GameServer;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component("bandastation")
public class BandaStationApiClientImpl extends AbstractGameApiClient<BandaStationServerStatusDTO> {

    @Override
    public Mono<BandaStationServerStatusDTO> getServerStatus(GameServer gameServer) {
        return callServer(gameServer, "status", new TypeReference<>() {});
    }

    @Override
    public Mono<List<AdminStatusDto>> getAdminsList(GameServer gameServer) {
        // TODO: Implement admin list fetching
        throw new UnsupportedOperationException("Admin list fetching is not implemented yet");
    }
}
