package club.ss220.manager.app.command.common;

import club.ss220.manager.app.controller.OnlineController;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command
@AllArgsConstructor
public class OnlineCommand extends ApplicationCommand {

    private final OnlineController onlineController;

    @JDASlashCommand(name = "online", description = "Показать текущий онлайн.")
    public void onSlashInteraction(GuildSlashEvent event) {
        log.debug("Executing /online command");
        event.deferReply(true).queue();

        onlineController.showPlayersOnline(event.getHook());
    }
}
