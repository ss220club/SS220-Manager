package club.ss220.manager.app.command.dev;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.service.GameServerService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Command
@AllArgsConstructor
public class DebugCommand extends ApplicationCommand {

    private final GameServerService gameServerService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "debug", description = "Отладочные данные от игрового сервера.")
    @TopLevelSlashCommandData(defaultLocked = true)
    public void onSlashInteraction(GuildSlashEvent event,
                                   @SlashOption(description = "Игровой сервер.", usePredefinedChoices = true)
                                   GameServer server) {
        event.deferReply().queue();

        InteractionHook hook = event.getHook();
        GameServerStatus serverStatus = gameServerService.getServerStatus(server);
        senders.sendEmbed(hook, embeds.serverStatus(server, serverStatus));
    }
}
