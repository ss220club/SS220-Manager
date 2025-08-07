package club.ss220.manager.app.controller;

import club.ss220.core.model.GameServer;
import club.ss220.core.model.GameServerStatus;
import club.ss220.core.service.GameServerService;
import club.ss220.manager.app.view.DebugView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class DebugController {

    private final DebugView view;
    private final GameServerService gameServerService;

    public void showServerDebugInfo(InteractionHook hook, GameServer server) {
        try {
            GameServerStatus serverStatus = gameServerService.getServerStatus(server);
            view.renderServerStatus(hook, server, serverStatus);

            log.debug("Displayed debug info for server {}", server.getFullName());
        } catch (Exception e) {
            throw new RuntimeException("Error displaying server debug info for server " + server, e);
        }
    }
}
