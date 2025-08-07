package club.ss220.manager.app.command.dev;

import club.ss220.manager.app.controller.StatusController;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command
@AllArgsConstructor
public class StatusCommand extends ApplicationCommand {

    private final StatusController statusController;

    @JDASlashCommand(name = "status", description = "Показать статус бота.")
    @TopLevelSlashCommandData(defaultLocked = true)
    public void onSlashInteraction(GuildSlashEvent event) {
        log.debug("Executing /status command");

        boolean ephemeral = true;
        event.deferReply(ephemeral).queue();
        statusController.showApplicationStatus(event.getHook(), event.getGuild());
    }
}
