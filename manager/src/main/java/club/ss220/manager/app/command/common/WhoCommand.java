package club.ss220.manager.app.command.common;

import club.ss220.manager.app.controller.WhoController;
import club.ss220.manager.model.GameServer;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command
@AllArgsConstructor
public class WhoCommand extends ApplicationCommand {

    private final WhoController whoController;

    @JDASlashCommand(name = "who", description = "Показать список игроков на сервере.")
    public void onSlashInteraction(GuildSlashEvent event,
                                   @SlashOption(description = "Игровой сервер.", usePredefinedChoices = true)
                                   GameServer server) {
        event.deferReply().queue();
        whoController.showPlayersOnServer(event.getHook(), server);
    }
}
