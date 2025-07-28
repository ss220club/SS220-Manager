package club.ss220.manager.app.command.dev;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.ApplicationStatus;
import club.ss220.manager.service.ApplicationStatusService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Slf4j
@Command
public class StatusCommand extends ApplicationCommand {

    private final ApplicationStatusService applicationStatusService;
    private final Embeds embeds;
    private final Senders senders;

    public StatusCommand(ApplicationStatusService applicationStatusService, Embeds embeds, Senders senders) {
        this.applicationStatusService = applicationStatusService;
        this.embeds = embeds;
        this.senders = senders;
    }

    @JDASlashCommand(name = "status", description = "Показать статус бота.")
    public void onSlashInteraction(GuildSlashEvent event) {
        event.deferReply().queue();
        InteractionHook hook = event.getHook();

        try {
            JDA jda = event.getJDA();
            ApplicationStatus applicationStatus = applicationStatusService.getApplicationStatus(jda);
            senders.sendEmbed(hook, embeds.applicationStatus(applicationStatus));
        } catch (Exception e) {
            log.error("Error building status embed", e);
            senders.sendEmbed(hook, embeds.error("Произошла ошибка при получении статуса бота"));
        }
    }
}
