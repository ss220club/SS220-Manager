package club.ss220.manager.app.command.common;

import club.ss220.manager.app.controller.AdminsController;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command
@AllArgsConstructor
public class AdminsCommand extends ApplicationCommand {

    private final AdminsController adminsController;

    @JDASlashCommand(name = "admins", description = "Показать админов онлайн.")
    public void onSlashInteraction(GuildSlashEvent event) {
        log.debug("Executing /admins command");

        boolean ephemeral = true;
        event.deferReply(ephemeral).queue();
        adminsController.showOnlineAdmins(event.getHook());
    }
}
