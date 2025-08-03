package club.ss220.manager.app.command.common;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.service.GameServerService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Command
@AllArgsConstructor
public class WhoCommand extends ApplicationCommand {

    private final GameServerService gameServerService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "who", description = "Показать список игроков на сервере.")
    public void onSlashInteraction(GuildSlashEvent event,
                                   @SlashOption(description = "Игровой сервер.", usePredefinedChoices = true)
                                   GameServer server) {
        event.deferReply().queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event.getHook());
        getPlayersOnline(server, onSuccess);
    }

    private void getPlayersOnline(GameServer server, Consumer<MessageEmbed> onSuccess) {
        List<String> playersOnline = gameServerService.getPlayersList(server);
        onSuccess.accept(embeds.playersOnline(server, playersOnline));
    }
}
