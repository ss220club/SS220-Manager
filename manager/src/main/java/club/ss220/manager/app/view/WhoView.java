package club.ss220.manager.app.view;

import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class WhoView {

    private final Senders senders;

    public void renderPlayersOnline(InteractionHook hook, GameServer gameServer, List<String> playersOnline) {
        renderPlayersOnlineForServer(hook, gameServer, playersOnline);
    }

    public void renderPlayersOnlineForServer(InteractionHook hook, GameServer gameServer, List<String> playersOnline) {
        MessageEmbed embed = createPlayersOnlineForServerEmbed(gameServer, playersOnline);
        senders.sendEmbed(hook, embed);
    }

    private MessageEmbed createPlayersOnlineForServerEmbed(GameServer gameServer, List<String> playersOnline) {
        String description = playersOnline.stream()
                .sorted(String::compareTo)
                .collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setTitle("Текущий онлайн: " + playersOnline.size())
                .setDescription(description)
                .setFooter(gameServer.getFullName())
                .setColor(UiConstants.COLOR_INFO)
                .build();
    }
}
