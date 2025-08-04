package club.ss220.manager.app.view;

import club.ss220.manager.app.util.Embeds;
import club.ss220.manager.app.util.Formatters;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameBuild;
import club.ss220.manager.model.GameCharacter;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class CharacterView {

    private final Embeds embeds;
    private final Senders senders;
    private final Formatters formatters;

    public void renderCharactersInfo(InteractionHook hook, List<GameCharacter> characters) {
        MessageEmbed embed = createCharactersInfoEmbed(characters);
        senders.sendEmbed(hook, embed);
    }

    public void renderNoCharactersFound(InteractionHook hook, String query) {
        MessageEmbed embed = embeds.error("Персонажи по запросу '" + query + "' не найдены.");
        senders.sendEmbed(hook, embed);
    }

    public void renderUnsupportedBuild(InteractionHook hook, GameBuild build) {
        MessageEmbed embed = embeds.error(build.getName() + " пока не поддерживает поиск персонажей.");
        senders.sendEmbed(hook, embed);
    }

    private MessageEmbed createCharactersInfoEmbed(List<GameCharacter> characters) {
        List<MessageEmbed.Field> fields = characters.stream().limit(MessageEmbed.MAX_FIELD_AMOUNT)
                .map(character -> new MessageEmbed.Field(character.getRealName(), character.getCkey(), true))
                .toList();

        EmbedBuilder embed = new EmbedBuilder().setTitle("Информация о персонажах");
        embed.getFields().addAll(fields);

        if (characters.size() > fields.size()) {
            String format = "Еще {0, plural,"
                            + " one{# персонаж не отображен}"
                            + " few{# персонажа не отображено}"
                            + " many{# персонажей не отображено}"
                            + " other{# персонажей не отображено}}.";
            embed.setFooter(formatters.formatPlural(format, characters.size() - fields.size()));
        }

        embed.setColor(UiConstants.COLOR_INFO);
        return embed.build();
    }
}
