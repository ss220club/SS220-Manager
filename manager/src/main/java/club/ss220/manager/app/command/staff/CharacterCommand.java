package club.ss220.manager.app.command.staff;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.service.GameCharacterService;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Command
@AllArgsConstructor
public class CharacterCommand extends ApplicationCommand {

    private final GameCharacterService gameCharacterService;
    private final Embeds embeds;
    private final Senders senders;

    @JDASlashCommand(name = "character", description = "Узнать владельца персонажа.")
    @TopLevelSlashCommandData(defaultLocked = true)
    public void onSlashInteraction(GuildSlashEvent event,
                                   @SlashOption(usePredefinedChoices = true) GameBuild build,
                                   @SlashOption(description = "Имя персонажа или его часть.") String name) {
        event.deferReply().queue();

        Consumer<MessageEmbed> onSuccess = senders.sendEmbed(event.getHook());
        Consumer<String> onFail = m -> senders.sendEmbed(event.getHook(), embeds.error(m));

        try {
            getCharactersInfoByName(build, name, onSuccess, onFail);
        } catch (UnsupportedOperationException e) {
            // TODO: 01.08.2025 Remove this when bandastation will store game characters in a database.
            onFail.accept(build.getName() + " пока не поддерживает поиск персонажей.");
        }
    }

    private void getCharactersInfoByName(GameBuild gameBuild, String query,
                                         Consumer<MessageEmbed> onSuccess, Consumer<String> onFail) {
        List<GameCharacter> characters = gameCharacterService.getCharactersByName(gameBuild, query);
        if (characters.isEmpty()) {
            onFail.accept("Персонажи по запросу '" + query + "' не найдены.");
            return;
        }

        onSuccess.accept(embeds.charactersInfo(characters));
    }
}
