package club.ss220.manager.data.integration.game.impl.paradise;

import club.ss220.manager.data.integration.game.impl.AbstractGameApiClient;
import club.ss220.manager.model.GameServer;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component("paradise")
public class ParadiseApiClientImpl extends AbstractGameApiClient<ParadiseServerStatusDto> {

    @Override
    public Mono<ParadiseServerStatusDto> getServerStatus(GameServer gameServer) {
        return callServer(gameServer, "status", new TypeReference<>() {});
    }

    @Override
    public Mono<List<AdminStatusDto>> getAdminsList(GameServer gameServer) {
        return callServer(gameServer, "adminwho", new TypeReference<>() {});
    }
}
