package club.ss220.manager.app.util;

import club.ss220.manager.app.pagination.PaginationData;
import club.ss220.manager.model.ApplicationStatus;
import club.ss220.manager.model.Ban;
import club.ss220.manager.model.GameCharacter;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import club.ss220.manager.model.OnlineAdminStatus;
import club.ss220.manager.model.Player;
import club.ss220.manager.model.RoleCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ibm.icu.text.MessageFormat;
import dev.freya02.jda.emojis.unicode.Emojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.awt.Color;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Embeds {

    public static final Color COLOR_ERROR = new Color(220, 53, 69);
    public static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    public static final Color COLOR_INFO = new Color(72, 115, 158);

    public static final String SPACE_FILLER = "\u3164    ";

    private final Formatters formatters;
    private final ObjectMapper objectMapper;

    public Embeds(Formatters formatters) {
        this.formatters = formatters;
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
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

    public MessageEmbed serverStatus(GameServer server, GameServerStatus serverStatus) {
        return new EmbedBuilder().setTitle("Статус сервера " + server.getFullName())
                .setDescription(serverStatusBlock(serverStatus))
                .setColor(COLOR_INFO)
                .build();
    }

    private String serverStatusBlock(GameServerStatus serverStatus) {
        try {
            return "```json\n" + objectMapper.writeValueAsString(serverStatus.getRawData()) + "\n```";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageEmbed userInfo(club.ss220.manager.model.User user) {
        return userInfo(user, false);
    }

    public MessageEmbed userInfo(club.ss220.manager.model.User user, boolean isConfidential) {
        Player player = user.getGameInfo().firstEntry().getValue();
        String description = "**Discord:** " + User.fromId(user.getDiscordId()).getAsMention() + "\n"
                             + "**CKEY:** " + user.getCkey() + "\n\n"
                             + playerInfoBlock(player, isConfidential);
        List<MessageEmbed.Field> fields = List.of(playerExpField(player),
                                                  playerCharactersField(player.getCharacters()));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Информация о пользователе " + user.getId());
        embed.setDescription(description);
        embed.getFields().addAll(fields);
        if (isConfidential) {
            embed.setFooter("Осторожно, сообщение содержит конфиденциальные данные.");
        }
        embed.setColor(COLOR_INFO);
        return embed.build();
    }

    private String playerInfoBlock(Player player, boolean isConfidential) {
        String info = "**Ранг:** " + player.getLastAdminRank() + "\n"
                      + "**Стаж:** " + player.getKnownFor().toDays() + " дн.\n"
                      + "**BYOND создан:** " + formatters.formatDate(player.getByondJoinDate()) + "\n"
                      + "**Первый вход:** " + formatters.formatDateTime(player.getFirstSeenDateTime()) + "\n"
                      + "**Последний вход:** " + formatters.formatDateTime(player.getLastSeenDateTime()) + "\n";
        if (isConfidential) {
            info += "**IP:** ||" + player.getIp().getHostAddress() + "||\n"
                    + "**CID:** ||" + player.getComputerId() + "||\n";
        }
        return info;
    }

    private MessageEmbed.Field playerExpField(Player player) {
        Duration livingExp = player.getExp().get(RoleCategory.LIVING);
        Duration ghostExp = player.getExp().get(RoleCategory.GHOST);
        Duration totalExp = livingExp.plus(ghostExp);

        String title = "Время в игре: " + totalExp.toHours() + " ч.";
        String value = player.getExp().entrySet().stream()
                .filter(e -> !e.getKey().equals(RoleCategory.IGNORE))
                .map(e -> playerExpLine(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, true);
    }

    private String playerExpLine(RoleCategory category, Duration exp) {
        return SPACE_FILLER.repeat(category.getLevel()) + category.getFormattedName() + ": " + exp.toHours() + " ч.";
    }

    private MessageEmbed.Field playerCharactersField(@Nullable List<GameCharacter> characters) {
        if (characters == null) {
            String value = Emojis.WARNING.getFormatted() + " Информация о персонажах недоступна.";
            return new MessageEmbed.Field("Персонажи: 0", value, true);
        }

        String title = "Персонажи: " + characters.size();
        String value = characters.stream()
                .map(this::playerCharacterLine)
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, true);
    }

    private String playerCharacterLine(GameCharacter character) {
        String ageFormat = "{0, plural, one{# год} few{# года} many{# лет} other{# лет}}.";
        return "`%02d` %s\n%s %s %s".formatted(
                character.getSlot(), character.getRealName(),
                genderEmoji(character.getGender()).getFormatted(),
                character.getSpecies().getName(),
                MessageFormat.format(ageFormat, character.getAge()));
    }

    private Emoji genderEmoji(GameCharacter.Gender gender) {
        return switch (gender) {
            case MALE -> Emojis.MALE_SIGN;
            case FEMALE -> Emojis.FEMALE_SIGN;
            case PLURAL -> Emojis.PARKING;
            case OTHER -> Emojis.HELICOPTER;
        };
    }

    public MessageEmbed charactersInfo(List<GameCharacter> characters) {
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
        Function<GameServer, GameBuildStyle> serverToStyle = e -> GameBuildStyle.fromName(e.getBuild().getName());
        return map.entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> serverToStyle.apply(e.getKey()),
                        TreeMap::new,
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
    }

    private MessageEmbed.Field buildAdminsField(GameBuildStyle buildStyle,
                                                Map<GameServer, List<OnlineAdminStatus>> serversAdmins) {
        String title = buildStyle.getEmoji().getFormatted() + " " + buildStyle.getName();
        String value = serversAdmins.entrySet().stream()
                .map(e -> serverAdminsBlock(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
        return new MessageEmbed.Field(title, value, false);
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
        return "**%s:** %d (%d) - %s".formatted(
                server.getName(),
                status.getPlayers(),
                status.getAdmins(),
                formatters.formatRoundDuration(status.getRoundDuration())
        );
    }

    public MessageEmbed playersOnline(GameServer gameServer, List<String> playersOnline) {
        String description = playersOnline.stream()
                .sorted(String::compareTo)
                .collect(Collectors.joining(", "));

        return new EmbedBuilder()
                .setTitle("Текущий онлайн: " + playersOnline.size())
                .setDescription(description)
                .setFooter(gameServer.getFullName())
                .setColor(COLOR_INFO)
                .build();
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
