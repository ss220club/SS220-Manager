package club.ss220.manager.app.command.staff;

import club.ss220.manager.app.controller.MemberInfoController;
import club.ss220.manager.model.MemberTarget;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand;
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

@Slf4j
@Command
@AllArgsConstructor
public class PlayerInfoCommand extends ApplicationCommand {

    private final MemberInfoController memberInfoController;

    @JDASlashCommand(name = "player", description = "Получить информацию об игроке.")
    @TopLevelSlashCommandData(defaultLocked = true)
    public void onSlashInteraction(GuildSlashEvent event,
                                   @SlashOption(description = "Пользователь Discord/Discord ID/CKEY.") String target) {
        log.debug("Executing /player command, target: {}", target);
        event.deferReply(true).queue();

        User user = event.getUser();
        memberInfoController.showMemberInfo(event.getHook(), user, MemberTarget.fromQuery(target));
    }

    @JDAUserCommand(name = "Информация об игроке", scope = CommandScope.GUILD)
    public void onUserInteraction(GuildUserEvent event) {
        User target = event.getTarget();
        log.debug("Executing user interaction 'Информация об игроке', target: {}", target);
        event.deferReply(true).queue();

        User user = event.getUser();
        memberInfoController.showMemberInfo(event.getHook(), user, target);
    }
}
