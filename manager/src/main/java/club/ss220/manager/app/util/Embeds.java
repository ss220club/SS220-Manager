package club.ss220.manager.app.util;

import club.ss220.manager.app.pagination.PaginationData;
import club.ss220.manager.model.ApplicationStatus;
import club.ss220.manager.model.Ban;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdminStatus;
import club.ss220.manager.model.Player;
import dev.freya02.jda.emojis.unicode.Emojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Embeds {

    public static final Color COLOR_ERROR = new Color(220, 53, 69);
    public static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    public static final Color COLOR_INFO = new Color(72, 115, 158);

    public static final String SPACE_FILLER = "\u3164    ";

    private final Formatters formatters;

    public Embeds(Formatters formatters) {
        this.formatters = formatters;
    }

    public MessageEmbed error(String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Ошибка");
        embed.setDescription(message);
        embed.setColor(COLOR_ERROR);
        return embed.build();
    }

    public MessageEmbed uncaughtException(String message, LinkedHashMap<String, Object> context) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Отчет об ошибке");
        embed.setDescription(message);
        context.forEach((key, value) -> embed.addField(key, String.valueOf(value), false));
        embed.setFooter("К сообщению прикреплен файл со стеком вызовов.");
        embed.setColor(COLOR_ERROR);
        return embed.build();
    }

    public MessageEmbed applicationStatus(ApplicationStatus applicationStatus) {
        EmbedBuilder embed = new EmbedBuilder();
        ApplicationStatus.Level summaryLevel = applicationStatus.getSummaryLevel();
        embed.setTitle(mapStatusLevel(summaryLevel).getFormatted() + " " + summaryLevel.getDescription());

        ApplicationStatus.Level latencyStatus = applicationStatus.getLatencyLevel();
        ApplicationStatus.Level persistenceLevel = applicationStatus.getPersistenceLevel();

        StringBuilder description = new StringBuilder()
                .append(applicationStatusLine("Revision", applicationStatus.revision()))
                .append(applicationStatusLine("Uptime", formatters.formatDuration(applicationStatus.uptime())))
                .append(applicationStatusLine("Profiles", applicationStatus.profiles().toString()))
                .append(applicationStatusLine("Global commands", applicationStatus.globalCommands()))
                .append(applicationStatusLine("Guild commands", applicationStatus.guildCommands()))
                .append(applicationStatusLine(latencyStatus, "Latency", applicationStatus.latency() + "ms"))
                .append(applicationStatusLine(persistenceLevel, "Persistence", applicationStatus.persistenceStatus()))
                .append("\n");

        ApplicationStatus.Level threadLevel = applicationStatus.getThreadLevel();
        long heapUsed = applicationStatus.heapUsed();
        long heapMax = applicationStatus.heapMax();
        double heapRatio = heapMax != 0 ? (double) heapUsed / heapMax : 0;
        ApplicationStatus.Level memoryLevel = applicationStatus.getMemoryLevel();

        description.append(applicationStatusLine("Java", applicationStatus.javaVersion()))
                .append(applicationStatusLine(threadLevel, "Threads", applicationStatus.threadCount()))
                .append(applicationStatusLine(memoryLevel, "Heap", "%s MB / %s MB (%.2f%%)".formatted(
                        DataSize.ofBytes(heapUsed).toMegabytes(),
                        DataSize.ofBytes(heapMax).toMegabytes(),
                        heapRatio * 100)));

        embed.setDescription(description.toString());
        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    private String applicationStatusLine(ApplicationStatus.Level level, String label, Object value) {
        return applicationStatusLine(mapStatusLevel(level), label, value);
    }

    private String applicationStatusLine(String label, Object value) {
        return applicationStatusLine(Emojis.WHITE_CIRCLE, label, value);
    }

    private String applicationStatusLine(Emoji statusEmoji, String label, Object value) {
        return "%s %s: %s\n".formatted(statusEmoji.getFormatted(), label, value);
    }

    public MessageEmbed playerInfo(Player player) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Информация о игроке " + player.getCkey());

        if (player.getFirstSeen() != null) {
            LocalDateTime firstSeen = player.getFirstSeen();
            embed.addField("Первый вход", formatters.formatDateTime(firstSeen), true);
        }
        if (player.getLastSeen() != null) {
            LocalDateTime lastSeen = player.getLastSeen();
            embed.addField("Последний вход", formatters.formatDateTime(lastSeen), true);
        }
        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    public MessageEmbed charactersInfo(List<GameCharacter> characters) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Информация о персонажах");

        String names = characters.stream().map(GameCharacter::getRealName).collect(Collectors.joining("\n"));
        embed.addField("Имя персонажа", names, true);
        String ckeys = characters.stream().map(GameCharacter::getCkey).collect(Collectors.joining("\n"));
        embed.addField("ckey", ckeys, true);
        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    public MessageEmbed onlineAdmins(Map<GameServer, List<OnlineAdminStatus>> serversAdmins) {
        List<MessageEmbed.Field> fields = groupByBuildStyle(serversAdmins).entrySet().stream()
                .map(e -> buildAdminsField(e.getKey(), e.getValue()))
                .toList();

        long totalAdmins = serversAdmins.values().stream()
                .flatMap(Collection::stream)
                .map(OnlineAdminStatus::getCkey)
                .distinct()
                .count();

        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Админы онлайн: " + totalAdmins);
        embedBuilder.getFields().addAll(fields);
        embedBuilder.setColor(COLOR_INFO);
        return embedBuilder.build();
    }

    private <V> Map<GameBuildStyle, Map<GameServer, V>> groupByBuildStyle(Map<GameServer, V> map) {
        return map.entrySet().stream()
                .sorted(Comparator.comparing(e -> GameBuildStyle.fromName(e.getKey().getBuild())))
                .collect(Collectors.groupingBy(
                        e -> GameBuildStyle.fromName(e.getKey().getBuild()),
                        LinkedHashMap::new,
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
    }

    private MessageEmbed.Field buildAdminsField(GameBuildStyle buildStyle,
                                                Map<GameServer, List<OnlineAdminStatus>> serversAdmins) {
        String title = buildStyle.getEmoji().getFormatted() + " " + buildStyle.getName();
        String value = serversAdmins.entrySet().stream()
                .map(e -> serverAdminsBlock(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, true);
    }

    private String serverAdminsBlock(GameServer server, List<OnlineAdminStatus> admins) {
        StringBuilder builder = new StringBuilder();
        builder.append("**").append(server.getName()).append("**\n");
        if (admins.isEmpty()) {
            builder.append(SPACE_FILLER + "Нет админов онлайн.");
            return builder.toString();
        }
        admins.forEach(a -> {
            String ranks = String.join(", ", a.getRanks());
            builder.append(SPACE_FILLER).append(a.getKey()).append(" - ").append(ranks).append("\n");
        });
        return builder.toString().trim();
    }

    public MessageEmbed playersOnline(Map<GameServer, GameServerStatus> serversStatuses) {
        List<MessageEmbed.Field> fields = groupByBuildStyle(serversStatuses).entrySet().stream()
                .map(e -> buildOnlineField(e.getKey(), e.getValue()))
                .toList();

        int totalPlayers = serversStatuses.values().stream().mapToInt(GameServerStatus::getPlayers).sum();

        EmbedBuilder embed = new EmbedBuilder().setTitle("Текущий онлайн: " + totalPlayers);
        embed.getFields().addAll(fields);
        embed.setFooter("(*) - администрация");
        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    private MessageEmbed.Field buildOnlineField(GameBuildStyle buildStyle, Map<GameServer, GameServerStatus> servers) {
        String title = buildStyle.getEmoji().getFormatted() + " **" + buildStyle.getName() + "**";
        String value = servers.entrySet().stream()
                .map(e -> serverOnlineBlock(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, false);
    }

    private String serverOnlineBlock(GameServer server, GameServerStatus status) {
        return "**%s**: %d (%d) - %s".formatted(
                server.getName(),
                status.getPlayers(),
                status.getAdmins(),
                formatters.formatRoundDuration(status.getRoundDuration())
        );
    }

    public MessageEmbed paginatedBanList(PaginationData<Ban> paginationData) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(paginationData.title());

        if (paginationData.items().isEmpty()) {
            embed.setDescription("У данного игрока нет блокировок.");
            embed.setColor(COLOR_SUCCESS);
            return embed.build();
        }

        String description = paginationData.items().stream()
                .map(ban -> banBlock(ban) + "\n")
                .collect(Collectors.joining())
                .trim();

        embed.setDescription(description);
        embed.setFooter("Всего: %d | Страница %d/%d".formatted(
                paginationData.totalItems(),
                paginationData.page() + 1,
                paginationData.getTotalPages()));
        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    public MessageEmbed banDetails(Ban ban) {
        String banDateTimeFormatted = formatters.formatDateTime(ban.getBanTime());
        String unbanDateTimeFormatted = Optional.ofNullable(ban.getUnbanTime())
                .map(formatters::formatDateTime).orElse("Бессрочно");

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("%s **Детали блокировки #%d**".formatted(Emojis.PROHIBITED.getFormatted(), ban.getId()));
        embed.setDescription(ban.getReason());
        embed.addField("Тип блокировки", ban.getBanType(), true);
        embed.addField("Нарушитель", ban.getCkey(), true);
        embed.addField("Админ", ban.getAdminCkey(), true);
        embed.addField("Статус", ban.isActive() ? "Активна" : "Снята", true);
        embed.addField("Время блокировки", banDateTimeFormatted, true);
        embed.addField("Время снятия блокировки", unbanDateTimeFormatted, true);

        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    private String banBlock(Ban ban) {
        String banType = ban.getBanType().toLowerCase().contains("job") ? "Блокировка роли" : "Блокировка";
        String statusText = ban.isActive() ? "Активна" : "Снята";
        UnicodeEmoji statusEmoji = ban.isActive() ? Emojis.RED_CIRCLE : Emojis.GREEN_CIRCLE;
        UnicodeEmoji expiredEmoji = ban.isExpired() ? Emojis.OCTAGONAL_SIGN : Emojis.WHITE_CHECK_MARK;
        String banDateTimeString = formatters.formatDateTime(ban.getBanTime());
        String unbanDateTimeString =
                Optional.ofNullable(ban.getUnbanTime()).map(formatters::formatDateTime).orElse("Бессрочно");

        return """
                %s **%s #%d** — %s
                %s %s
                %s %s — %s %s
                %s %s
                """.formatted(
                statusEmoji.getFormatted(), banType, ban.getId(), statusText,
                Emojis.COP.getFormatted(), ban.getAdminCkey(),
                Emojis.CALENDAR.getFormatted(), banDateTimeString, expiredEmoji.getFormatted(), unbanDateTimeString,
                Emojis.MEMO.getFormatted(), ban.getShortReason()
        ).trim();
    }

    private static UnicodeEmoji mapStatusLevel(ApplicationStatus.Level level) {
        return switch (level) {
            case OK -> Emojis.GREEN_CIRCLE;
            case WARNING -> Emojis.ORANGE_CIRCLE;
            case CRITICAL -> Emojis.RED_CIRCLE;
        };
    }
}
