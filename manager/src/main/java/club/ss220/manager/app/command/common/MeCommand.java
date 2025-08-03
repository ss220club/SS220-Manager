package club.ss220.manager.app.command.common;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.User;
import club.ss220.manager.service.UserService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Command
@AllArgsConstructor
public class MeCommand extends ApplicationCommand {

    private final UserService userService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "me", description = "Показать информацию о себе.")
    public void onSlashInteraction(GuildSlashEvent event) {
        event.deferReply(true).queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event.getHook());
        Consumer<String> onFail = m -> senders.sendEmbed(event.getHook(), embeds.error(m));
        net.dv8tion.jda.api.entities.User user = event.getUser();
        getPlayerInfo(user, onSuccess, onFail);
    }

    private void getPlayerInfo(net.dv8tion.jda.api.entities.User user,
                               Consumer<MessageEmbed> onSuccess, Consumer<String> onFail) {
        Optional<User> userOptional = userService.getUserByDiscordId(user.getIdLong());
        if (userOptional.isEmpty()) {
            onFail.accept("Пользователь " + user.getAsMention() + " не найден.");
            return;
        }

        onSuccess.accept(embeds.userInfo(userOptional.get()));
    }
}
