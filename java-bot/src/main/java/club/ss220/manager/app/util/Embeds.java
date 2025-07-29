package club.ss220.manager.app.util;

import club.ss220.manager.app.pagination.PaginationData;
import club.ss220.manager.model.ApplicationStatus;
import club.ss220.manager.model.Ban;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.OnlineAdmin;
import club.ss220.manager.model.OnlinePlayer;
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

        StringBuilder description = new StringBuilder();
        addApplicationStatusLine(description, "Revision", applicationStatus.getRevision());
        addApplicationStatusLine(description, "Uptime", formatters.formatDuration(applicationStatus.getUptime()));
        addApplicationStatusLine(description, "Profiles", applicationStatus.getProfiles().toString());
        addApplicationStatusLine(description, "Global commands", applicationStatus.getGlobalCommands());
        addApplicationStatusLine(description, "Guild commands", applicationStatus.getGuildCommands());
        ApplicationStatus.Level latencyStatus = applicationStatus.getLatencyLevel();
        addApplicationStatusLine(description, latencyStatus, "Latency", applicationStatus.getLatency() + "ms");
        ApplicationStatus.Level persistenceLevel = applicationStatus.getPersistenceLevel();
        addApplicationStatusLine(description, persistenceLevel, "Persistence", applicationStatus.isPersistenceStatus());

        description.append("\n");

        addApplicationStatusLine(description, "Java", applicationStatus.getJavaVersion());
        ApplicationStatus.Level threadLevel = applicationStatus.getThreadLevel();
        addApplicationStatusLine(description, threadLevel, "Threads", applicationStatus.getThreadCount());
        long heapUsed = applicationStatus.getHeapUsed();
        long heapMax = applicationStatus.getHeapMax();
        double heapRatio = heapMax != 0 ? (double) heapUsed / heapMax : 0;
        ApplicationStatus.Level memoryLevel = applicationStatus.getMemoryLevel();
        addApplicationStatusLine(description, memoryLevel, "Heap", "%s MB / %s MB (%.2f%%)".formatted(
                DataSize.ofBytes(heapUsed).toMegabytes(),
                DataSize.ofBytes(heapMax).toMegabytes(),
                heapRatio * 100));

        embed.setDescription(description.toString());
        embed.setColor(COLOR_INFO);
        return embed.build();
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

    public MessageEmbed onlineAdmins(Map<GameServer, List<OnlineAdmin>> serversAdminsMap) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Админы онлайн");

        StringBuilder description = new StringBuilder();
        for (Map.Entry<GameServer, List<OnlineAdmin>> entry : serversAdminsMap.entrySet()) {
            GameServer server = entry.getKey();
            List<OnlineAdmin> admins = entry.getValue();

            description.append("**").append(server.getName()).append(":**\n");
            for (OnlineAdmin admin : admins) {
                description.append("\t").append(admin.getKey()).append(" - ").append(admin.getRank()).append("\n");
            }
            description.append("\n");
        }
        embed.setDescription(description.toString());

        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    public MessageEmbed playersOnline(String serverName, List<OnlinePlayer> players) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Игроки онлайн на сервере: " + serverName);

        if (players.isEmpty()) {
            embed.setDescription("На сервере сейчас нет игроков");
        } else {
            StringBuilder playersList = new StringBuilder();
            for (OnlinePlayer player : players) {
                playersList.append("**").append(player.getCharacterName()).append("** (")
                        .append(player.getCkey()).append(") - ")
                        .append(player.getJob()).append("\n");
            }

            embed.setDescription(playersList.toString());
            embed.addField("Всего игроков", String.valueOf(players.size()), true);
        }

        embed.setColor(COLOR_INFO);
        return embed.build();
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

    private void addApplicationStatusLine(StringBuilder builder, ApplicationStatus.Level level,
                                          String label, Object value) {
        addApplicationStatusLine(builder, mapStatusLevel(level), label, value);
    }

    private void addApplicationStatusLine(StringBuilder builder, String label, Object value) {
        addApplicationStatusLine(builder, Emojis.WHITE_CIRCLE, label, value);
    }

    private void addApplicationStatusLine(StringBuilder builder, Emoji statusEmoji, String label, Object value) {
        builder.append(statusEmoji.getFormatted()).append(" ").append(label).append(": ").append(value).append("\n");
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
