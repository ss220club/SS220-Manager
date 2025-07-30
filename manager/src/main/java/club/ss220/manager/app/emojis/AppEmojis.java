package club.ss220.manager.app.emojis;

import io.github.freya022.botcommands.api.emojis.AppEmojisRegistry;
import io.github.freya022.botcommands.api.emojis.annotations.AppEmoji;
import io.github.freya022.botcommands.api.emojis.annotations.AppEmojiContainer;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;

@AppEmojiContainer
public class AppEmojis {
    @AppEmoji(emojiName = "bandastation")
    public static final ApplicationEmoji BANDASTATION = AppEmojisRegistry.get("BANDASTATION");
}
