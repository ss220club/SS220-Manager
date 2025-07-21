package club.ss220.manager.bot.util;

import club.ss220.manager.config.BotConfig;
import club.ss220.manager.db.paradise.entity.GameCharacter;
import club.ss220.manager.db.paradise.entity.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class Embeds {

    private final BotConfig botConfig;

    @Autowired
    private Embeds(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    public MessageEmbed playerInfo(Player player) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Информация о игроке " + player.getCkey());

        if (player.getFirstSeen() != null) {
            ZonedDateTime firstSeen = player.getFirstSeen().atZone(ZoneId.systemDefault());
            embed.addField("Первый вход", botConfig.dateTimeFormatter().format(firstSeen), true);
        }
        if (player.getLastSeen() != null) {
            ZonedDateTime lastSeen = player.getLastSeen().atZone(ZoneId.systemDefault());
            embed.addField("Последний вход", botConfig.dateTimeFormatter().format(lastSeen), true);
        }
        return embed.build();
    }

    public MessageEmbed charactersInfo(List<GameCharacter> characters) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Информация о персонажах");
        characters.forEach(character -> embed.addField(character.getRealName(), character.getCkey(), true));
        return embed.build();
    }
}
