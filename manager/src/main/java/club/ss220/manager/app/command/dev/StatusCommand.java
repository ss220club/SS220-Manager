package club.ss220.manager.app.command.dev;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.ApplicationStatus;
import club.ss220.manager.service.ApplicationStatusService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Command
@AllArgsConstructor
public class StatusCommand extends ApplicationCommand {

    private final ApplicationStatusService applicationStatusService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "status", description = "Показать статус бота.")
    @TopLevelSlashCommandData(defaultLocked = true)
    public void onSlashInteraction(GuildSlashEvent event) {
        event.deferReply().queue();

        InteractionHook hook = event.getHook();
        ApplicationStatus applicationStatus = applicationStatusService.getApplicationStatus(event.getGuild());
        senders.sendEmbed(hook, embeds.applicationStatus(applicationStatus));
    }
}
