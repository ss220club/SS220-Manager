package club.ss220.manager.app.view;

import club.ss220.manager.app.util.Formatters;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class OnlineView {

    private final Senders senders;
    private final Formatters formatters;

    public void renderPlayersOnline(InteractionHook hook, Map<GameServer, GameServerStatus> serversStatuses) {
        MessageEmbed embed = createPlayersOnlineEmbed(serversStatuses);
        senders.sendEmbed(hook, embed);
    }

    private MessageEmbed createPlayersOnlineEmbed(Map<GameServer, GameServerStatus> serversStatuses) {
        List<MessageEmbed.Field> fields = groupByBuildStyle(serversStatuses).entrySet().stream()
                .map(e -> buildOnlineField(e.getKey(), e.getValue()))
                .toList();

        int totalPlayers = serversStatuses.values().stream().mapToInt(GameServerStatus::getPlayers).sum();

        EmbedBuilder embed = new EmbedBuilder().setTitle("Текущий онлайн: " + totalPlayers);
        embed.getFields().addAll(fields);
        embed.setFooter("(*) - администрация");
        embed.setColor(UiConstants.COLOR_INFO);
        return embed.build();
    }

    private <V> Map<GameBuildStyle, Map<GameServer, V>> groupByBuildStyle(Map<GameServer, V> map) {
        Function<GameServer, GameBuildStyle> serverToStyle = e -> GameBuildStyle.fromName(e.getBuild().getName());
        return map.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> serverToStyle.apply(e.getKey()),
                        TreeMap::new,
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
    }

    private MessageEmbed.Field buildOnlineField(GameBuildStyle buildStyle, Map<GameServer, GameServerStatus> servers) {
        String title = buildStyle.getEmoji().getFormatted() + " **" + buildStyle.getName() + "**";
        String value = servers.entrySet().stream()
                .map(e -> serverOnlineBlock(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, false);
    }

    private String serverOnlineBlock(GameServer server, GameServerStatus status) {
        return "**%s:** %d (%d) - %s".formatted(
                server.getName(),
                status.getPlayers(),
                status.getAdmins(),
                formatters.formatRoundDuration(status.getRoundDuration())
        );
    }
}
