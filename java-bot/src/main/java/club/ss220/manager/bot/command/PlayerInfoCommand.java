package club.ss220.manager.bot.command;

import club.ss220.manager.bot.command.exception.CommandException;
import club.ss220.manager.bot.util.Embeds;
import club.ss220.manager.bot.util.Senders;
import club.ss220.manager.db.paradise.entity.Player;
import club.ss220.manager.service.PlayerService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand;
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Command
public class PlayerInfoCommand extends ApplicationCommand {

    private final PlayerService playerService;
    private final Embeds embeds;
    private final Senders senders;

    @Autowired
    public PlayerInfoCommand(PlayerService playerService, Embeds embeds, Senders senders) {
        this.playerService = playerService;
        this.embeds = embeds;
        this.senders = senders;
    }

    @JDASlashCommand(name = "игрок", description = "Получить информацию о игроке")
    public void onSlashInteraction(GuildSlashEvent event,
                                   @Nullable @SlashOption(description = "Пользователь Discord") User user,
                                   @Nullable @SlashOption(description = "Discord ID") Long discordId,
                                   @Nullable @SlashOption(description = "CKEY игрока") String ckey) {
        event.deferReply().queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event);
        Consumer<String> onFail = m -> senders.sendEmbed(event, embeds.error(m));
        try {
            if (user != null) {
                getPlayerInfoByUser(user, onSuccess, onFail);
                return;
            }
            if (discordId != null) {
                getPlayerInfoByDiscordId(discordId, onSuccess, onFail);
                return;
            }
            if (ckey != null) {
                getPlayerInfoByCkey(ckey, onSuccess, onFail);
                return;
            }
            onFail.accept("Укажите пользователя, Discord ID или CKEY.");
        } catch (Exception e) {
            onFail.accept("Произошла ошибка при получении информации об игроке.");
        }
    }

    @JDAUserCommand(name = "Информация об игроке", scope = CommandScope.GUILD)
    public void onUserInteraction(GuildUserEvent event) {
        event.deferReply().queue();

        User user = event.getUser();
        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event);
        Consumer<String> onFail = m -> senders.sendEmbed(event, embeds.error(m));
        try {
            getPlayerInfoByUser(user, onSuccess, onFail);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            onFail.accept("Произошла ошибка при получении информации об игроке.");
        }
    }

    private void getPlayerInfoByUser(User user, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail)
            throws CommandException {
        try {
            Optional<Player> playerOptional = playerService.getPlayerByDiscordId(user.getIdLong());
            if (playerOptional.isEmpty()) {
                onFail.accept("Игрок " + user.getAsMention() + " не найден.");
                return;
            }

            onSuccess.accept(embeds.playerInfo(playerOptional.get()));
        } catch (Exception e) {
            throw new CommandException("Error getting player info by user: " + user.getName(), e);
        }
    }

    private void getPlayerInfoByDiscordId(Long discordId, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail)
            throws CommandException {
        try {
            Optional<Player> playerOpt = playerService.getPlayerByDiscordId(discordId);
            if (playerOpt.isEmpty()) {
                onFail.accept("Игрок с Discord ID " + discordId + " не найден.");
                return;
            }

            Player player = playerOpt.get();
            onSuccess.accept(embeds.playerInfo(player));
        } catch (Exception e) {
            throw new CommandException("Error getting player info by Discord ID: " + discordId, e);
        }
    }

    private void getPlayerInfoByCkey(String ckey, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail)
            throws CommandException {
        try {
            Optional<Player> playerOptional = playerService.getPlayerByCkey(ckey);
            if (playerOptional.isEmpty()) {
                onFail.accept("Игрок с CKEY " + ckey + " не найден.");
                return;
            }

            onSuccess.accept(embeds.playerInfo(playerOptional.get()));
        } catch (Exception e) {
            throw new CommandException("Error getting player info by ckey: " + ckey, e);
        }
    }
}
