package club.ss220.manager.app.command.staff;

import club.ss220.manager.app.controller.CharacterController;
import club.ss220.manager.model.GameBuild;
import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command
@AllArgsConstructor
public class CharacterCommand extends ApplicationCommand {

    private final CharacterController characterController;

    @JDASlashCommand(name = "character", description = "Узнать владельца персонажа.")
    @TopLevelSlashCommandData(defaultLocked = true)
    public void onSlashInteraction(GuildSlashEvent event,
                                   @SlashOption(usePredefinedChoices = true) GameBuild build,
                                   @SlashOption(description = "Имя персонажа или его часть.") String name) {
        log.debug("Executing /character command, build: {}, name: '{}'", build.getName(), name);
        event.deferReply().queue();

        characterController.searchCharacters(event.getHook(), build, name);
    }
}
