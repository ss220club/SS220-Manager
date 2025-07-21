package club.ss220.manager.bot.commands;

import club.ss220.manager.bot.util.Embeds;
import club.ss220.manager.bot.util.Senders;
import club.ss220.manager.db.paradise.entity.Player;
import club.ss220.manager.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class PlayerInfoCommand extends AbstractCommand {

    private static final String SLASH_COMMAND_NAME = "игрок";
    private static final String SLASH_COMMAND_DESCRIPTION = "Получить информацию о игроке";
    private static final String CONTEXT_COMMAND_NAME = "Информация о игроке";

    private final PlayerService playerService;
    private final Embeds embeds;
    private final Senders senders;

    @Autowired
    public PlayerInfoCommand(PlayerService playerService, Embeds embeds, Senders senders) {
        super(SLASH_COMMAND_NAME, SLASH_COMMAND_DESCRIPTION);
        this.playerService = playerService;
        this.embeds = embeds;
        this.senders = senders;
    }

    @Override
    public List<CommandData> getCommandData() {
        List<CommandData> commands = new ArrayList<>();

        commands.add(Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, "ckey", "CKEY игрока", false)
                .addOption(OptionType.INTEGER, "discord", "Discord ID", false));

        commands.add(Commands.user(CONTEXT_COMMAND_NAME));
        return commands;
    }

    @Override
    public EventListener getListner() {
        return new EventHandler();
    }

    private void getPlayerInfoByCkey(String ckey, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail) {
        try {
            Optional<Player> playerOptional = playerService.getPlayerByCkey(ckey);
            if (playerOptional.isEmpty()) {
                onFail.accept("Игрок с CKEY " + ckey + " не найден");
                return;
            }

            onSuccess.accept(embeds.playerInfo(playerOptional.get()));
        } catch (Exception e) {
            log.error("Error getting player info by ckey: {}", ckey, e);
            onFail.accept("Ошибка при получении информации о игроке: " + e.getMessage());
        }
    }

    private void getPlayerInfoByDiscordId(Long discordId, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail) {
        try {
            Optional<Player> playerOpt = playerService.getPlayerByDiscordId(discordId);
            if (playerOpt.isEmpty()) {
                onFail.accept("Игрок с Discord ID " + discordId + " не найден");
                return;
            }

            Player player = playerOpt.get();
            onSuccess.accept(embeds.playerInfo(player));
        } catch (Exception e) {
            log.error("Error getting player info by Discord ID: {}", discordId, e);
            onFail.accept("Ошибка при получении информации о игроке: " + e.getMessage());
        }
    }

    private class EventHandler extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            event.deferReply().queue();

            OptionMapping ckeyOption = event.getOption("ckey");
            OptionMapping discordOption = event.getOption("discord");

            if (ckeyOption != null) {
                String ckey = ckeyOption.getAsString();
                getPlayerInfoByCkey(ckey, senders.sendEmbed(event), senders.sendMessage(event));
            } else if (discordOption != null) {
                long userId = discordOption.getAsLong();
                getPlayerInfoByDiscordId(userId, senders.sendEmbed(event), senders.sendMessage(event));
            } else {
                event.getHook().sendMessage("Укажите CKEY или Discord пользователя").queue();
            }
        }

        @Override
        public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
            event.deferReply().queue();

            User user = event.getTarget();
            getPlayerInfoByDiscordId(user.getIdLong(), senders.sendEmbed(event), senders.sendMessage(event));
        }
    }
}
