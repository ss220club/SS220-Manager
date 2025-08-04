package club.ss220.manager.app.view;

import dev.freya02.jda.emojis.unicode.Emojis;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Getter
public enum GameBuildStyle {
    PARADISE("Paradise", Emojis.PALM_TREE),
    BANDASTATION("BandaStation", AppEmojis.BANDASTATION);

    private final String name;
    private final Emoji emoji;

    GameBuildStyle(String name, Emoji emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public static GameBuildStyle fromName(String name) {
        return Arrays.stream(GameBuildStyle.values())
                .filter(style -> style.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Unknown game build style: " + name));
    }
}
