package club.ss220.manager.app.controller;

import club.ss220.core.model.GameServer;
import club.ss220.core.service.GameServerService;
import club.ss220.manager.app.view.WhoView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class WhoController {

    private final WhoView view;
    private final GameServerService gameServerService;

    public void showPlayersOnServer(InteractionHook hook, GameServer server) {
        try {
            List<String> playersOnline = gameServerService.getPlayersList(server);
            view.renderPlayersOnline(hook, server, playersOnline);
            
            log.debug("Displayed {} players on server {}", playersOnline.size(), server.getFullName());
        } catch (Exception e) {
            throw new RuntimeException("Error displaying players list for server " + server, e);
        }
    }
}
