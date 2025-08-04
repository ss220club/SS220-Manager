package club.ss220.manager.app.controller;

import club.ss220.manager.app.view.CharacterView;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.service.GameCharacterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class CharacterController {

    private final CharacterView view;
    private final GameCharacterService gameCharacterService;

    public void searchCharacters(InteractionHook hook, GameBuild build, String name) {
        try {
            List<GameCharacter> characters = gameCharacterService.getCharactersByName(build, name);
            if (characters.isEmpty()) {
                view.renderNoCharactersFound(hook, name);
                log.debug("Found 0 characters for query '{}', build {}", name, build.getName());
                return;
            }
            
            view.renderCharactersInfo(hook, characters);

            log.debug("Displayed {} characters for query '{}', build {}", characters.size(), name, build.getName());
        } catch (UnsupportedOperationException e) {
            log.warn("Character search not supported for build {}", build.getName());
            view.renderUnsupportedBuild(hook, build);
        } catch (Exception e) {
            log.error("Error searching characters for query '{}', build {}", name, build.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
