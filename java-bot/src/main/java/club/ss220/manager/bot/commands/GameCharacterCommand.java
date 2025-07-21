package club.ss220.manager.bot.commands;

import club.ss220.manager.bot.util.Embeds;
import club.ss220.manager.db.paradise.entity.GameCharacter;
import club.ss220.manager.service.GameCharacterService;
import club.ss220.manager.bot.util.Senders;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.util.function.Consumer;

@Slf4j
@Component
public class GameCharacterCommand extends AbstractCommand {

    private static final String SLASH_COMMAND_NAME = "персонажи";
    private static final String SLASH_COMMAND_DESCRIPTION = "Получить информацию о персонажах";
    private static final String CONTEXT_COMMAND_NAME = "Информация о персонажах";

    private final GameCharacterService gameCharacterService;
    private final Embeds embeds;
    private final Senders senders;

    @Autowired
    public GameCharacterCommand(GameCharacterService gameCharacterService, Embeds embeds, Senders senders) {
        super(SLASH_COMMAND_NAME, SLASH_COMMAND_DESCRIPTION);
        this.gameCharacterService = gameCharacterService;
        this.embeds = embeds;
        this.senders = senders;
    }

    @Override
    public List<CommandData> getCommandData() {
        List<CommandData> commands = new ArrayList<>();

        commands.add(Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, "ckey", "CKEY игрока", false)
                .addOption(OptionType.STRING, "name", "Имя персонажа", false));

        commands.add(Commands.user(CONTEXT_COMMAND_NAME));
        return commands;
    }

    @Override
    public EventListener getListner() {
        return new EventHandler();
    }

    private void getCharactersInfoByCkey(String ckey, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail) {
        try {
            List<GameCharacter> characters = gameCharacterService.getCharactersByCkey(ckey);
            if (characters.isEmpty()) {
                onFail.accept("Персонажи игрока с CKEY " + ckey + " не найдены");
                return;
            }

            onSuccess.accept(embeds.charactersInfo(characters));
        } catch (Exception e) {
            log.error("Error getting characters info by ckey: {}", ckey, e);
            onFail.accept("Ошибка при получении информации о персонажах: " + e.getMessage());
        }
    }

    private void getCharactersInfoByName(String characterName, Consumer<MessageEmbed> onSuccess, Consumer<String> onFail) {
        try {
            List<GameCharacter> characters = gameCharacterService.getCharactersByName(characterName);
            if (characters.isEmpty()) {
                onFail.accept("Персонажи с именем " + characterName + " не найдены");
                return;
            }

            onSuccess.accept(embeds.charactersInfo(characters));
        } catch (Exception e) {
            log.error("Error getting characters info by name: {}", characterName, e);
            onFail.accept("Ошибка при получении информации о персонажах: " + e.getMessage());
        }
    }

    private class EventHandler extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
            event.deferReply().queue();

            OptionMapping ckeyOption = event.getOption("ckey");
            OptionMapping characterNameOption = event.getOption("name");

            if (ckeyOption != null) {
                String ckey = ckeyOption.getAsString();
                getCharactersInfoByCkey(ckey, senders.sendEmbed(event), senders.sendMessage(event));
            } else if (characterNameOption != null) {
                String characterName = characterNameOption.getAsString();
                getCharactersInfoByName(characterName, senders.sendEmbed(event), senders.sendMessage(event));
            } else {
                event.getHook().sendMessage("Укажите CKEY или имя персонажа").queue();
            }
        }
    }
}
