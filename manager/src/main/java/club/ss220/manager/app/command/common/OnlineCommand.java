package club.ss220.manager.app.command.common;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.service.GameServerService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Command
@AllArgsConstructor
public class OnlineCommand extends ApplicationCommand {

    private final GameServerService gameServerService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "online", description = "Показать текущий онлайн.")
    public void onSlashInteraction(GuildSlashEvent event) {
        event.deferReply().queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event.getHook());
        getPlayersOnline(onSuccess);
    }

    private void getPlayersOnline(Consumer<MessageEmbed> onSuccess) {
        Map<GameServer, GameServerStatus> serversStatuses = gameServerService.getAllServersStatus();
        onSuccess.accept(embeds.playersOnline(serversStatuses));
    }
}
