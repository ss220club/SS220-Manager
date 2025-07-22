package club.ss220.manager.bot.command;

import club.ss220.manager.bot.command.exception.CommandException;
import club.ss220.manager.bot.util.Embeds;
import club.ss220.manager.db.paradise.entity.GameCharacter;
import club.ss220.manager.service.GameCharacterService;
import club.ss220.manager.bot.util.Senders;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Command
public class GameCharacterCommand extends ApplicationCommand {

    private final GameCharacterService gameCharacterService;
    private final Embeds embeds;
    private final Senders senders;

    @Autowired
    public GameCharacterCommand(GameCharacterService gameCharacterService, Embeds embeds, Senders senders) {
        this.gameCharacterService = gameCharacterService;
        this.embeds = embeds;
        this.senders = senders;
    }

    @JDASlashCommand(name = "персонажи", description = "Получить информацию о персонажах")
    public void onSlashInteraction(GuildSlashEvent event, @SlashOption(description = "Имя персонажа") String name) {
        event.deferReply().queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event);
        Consumer<String> onFail = senders.sendMessage(event);
        try {
            getCharactersInfoByName(name, onSuccess, onFail);
        } catch (CommandException e) {
            log.error(e.getMessage(), e);
            onFail.accept("Произошла ошибка при получении информации о персонажах.");
        }
    }

    private void getCharactersInfoByName(String characterName, Consumer<MessageEmbed> onSuccess,
                                         Consumer<String> onFail) throws CommandException {
        try {
            List<GameCharacter> characters = gameCharacterService.getCharactersByName(characterName);
            if (characters.isEmpty()) {
                onFail.accept("Персонажи с именем " + characterName + " не найдены");
                return;
            }

            onSuccess.accept(embeds.charactersInfo(characters));
        } catch (Exception e) {
            throw new CommandException("Error getting characters info by name: " + characterName, e);
        }
    }
}
