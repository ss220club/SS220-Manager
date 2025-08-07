package club.ss220.manager.app.controller;

import club.ss220.core.model.GameServer;
import club.ss220.core.model.GameServerStatus;
import club.ss220.core.service.GameServerService;
import club.ss220.manager.app.view.OnlineView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class OnlineController {

    private final OnlineView view;
    private final GameServerService gameServerService;

    public void showPlayersOnline(InteractionHook hook) {
        try {
            Map<GameServer, GameServerStatus> serversStatuses = gameServerService.getAllServersStatus();
            view.renderPlayersOnline(hook, serversStatuses);

            log.debug("Displayed online players for {} servers", serversStatuses.size());
        } catch (Exception e) {
            throw new RuntimeException("Error displaying online players", e);
        }
    }
}
