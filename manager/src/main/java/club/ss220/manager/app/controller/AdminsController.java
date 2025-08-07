package club.ss220.manager.app.controller;

import club.ss220.core.model.GameServer;
import club.ss220.core.model.OnlineAdminStatus;
import club.ss220.core.service.GameServerService;
import club.ss220.manager.app.view.AdminsView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class AdminsController {

    private final AdminsView view;
    private final GameServerService gameServerService;

    public void showOnlineAdmins(InteractionHook hook) {
        try {
            Map<GameServer, List<OnlineAdminStatus>> onlineAdmins = gameServerService.getAllAdminsList();
            view.renderOnlineAdmins(hook, onlineAdmins);

            log.debug("Displayed online admins for {} servers", onlineAdmins.size());
        } catch (Exception e) {
            throw new RuntimeException("Error displaying online admins", e);
        }
    }
}
