package club.ss220.manager.app.command.common;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.OnlineAdminStatus;
import club.ss220.manager.service.GameServerService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Command
@AllArgsConstructor
public class AdminsCommand extends ApplicationCommand {

    private final GameServerService gameServerService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "admins", description = "Показать админов онлайн")
    public void onSlashInteraction(GuildSlashEvent event) {
        event.deferReply().queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event.getHook());
        getOnlineAdmins(onSuccess);
    }

    private void getOnlineAdmins(Consumer<MessageEmbed> onSuccess) {
        Map<GameServer, List<OnlineAdminStatus>> onlineAdmins = gameServerService.getAllAdminsList();
        onSuccess.accept(embeds.onlineAdmins(onlineAdmins));
    }
}
