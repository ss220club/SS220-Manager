package club.ss220.manager.app.controller;

import club.ss220.manager.app.view.StatusView;
import club.ss220.manager.model.ApplicationStatus;
import club.ss220.manager.service.ApplicationStatusService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StatusController {

    private final StatusView view;
    private final ApplicationStatusService applicationStatusService;

    public void showApplicationStatus(InteractionHook hook, Guild guild) {
        try {
            ApplicationStatus applicationStatus = applicationStatusService.getApplicationStatus(guild);
            view.renderApplicationStatus(hook, applicationStatus);

            log.debug("Displayed application status for guild {}", guild.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error displaying application status for guild " + guild, e);
        }
    }
}
