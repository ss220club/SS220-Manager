package club.ss220.manager.app.command.common;

import club.ss220.manager.app.controller.MemberInfoController;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

@Slf4j
@Command
@AllArgsConstructor
public class MeCommand extends ApplicationCommand {

    private final MemberInfoController memberInfoController;

    @JDASlashCommand(name = "me", description = "Показать информацию о себе.")
    public void onSlashInteraction(GuildSlashEvent event) {
        log.debug("Executing /me command");

        boolean ephemeral = true;
        event.deferReply(ephemeral).queue();
        User discordUser = event.getUser();
        memberInfoController.showMemberInfo(event.getHook(), discordUser);
    }
}
