package club.ss220.manager.data.integration.game.impl.paradise;

import club.ss220.manager.data.integration.game.impl.AbstractGameApiClient;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdminStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class ParadiseApiClientImpl extends AbstractGameApiClient {

    @Override
    public GameServer.Build getBuild() {
        return GameServer.Build.PARADISE;
    }

    @Override
    public Mono<GameServerStatus> getServerStatus(GameServer gameServer) {
        return callServer(gameServer, "status", new TypeReference<ParadiseServerStatusDto>() {})
                .map(Function.identity());
    }

    @Override
    public Mono<List<OnlineAdminStatus>> getAdminsList(GameServer gameServer) {
        return callServer(gameServer, "adminwho", new TypeReference<List<ParadiseOnlineAdminStatusDTO>>() {})
                .map(List::copyOf);
    }
}
