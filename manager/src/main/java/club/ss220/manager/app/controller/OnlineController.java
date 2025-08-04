package club.ss220.manager.app.controller;

import club.ss220.manager.app.view.OnlineView;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.service.GameServerService;
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
            log.error("Error displaying players online", e);
            throw new RuntimeException(e);
        }
    }
}
