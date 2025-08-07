package club.ss220.manager.app.util;

import club.ss220.core.model.Ban;
import club.ss220.manager.app.pagination.PaginationData;
import club.ss220.manager.app.view.UiConstants;
import dev.freya02.jda.emojis.unicode.Emojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Embeds {

    private final Formatters formatters;

    public Embeds(Formatters formatters) {
        this.formatters = formatters;
    }

    public MessageEmbed error(String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Ошибка");
        embed.setDescription(message);
        embed.setColor(UiConstants.COLOR_ERROR);
        return embed.build();
    }

    public MessageEmbed uncaughtException(String message, LinkedHashMap<String, Object> context) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Отчет об ошибке");
        embed.setDescription(message);
        context.forEach((key, value) -> embed.addField(key, String.valueOf(value), false));
        embed.setFooter("К сообщению прикреплен файл со стеком вызовов.");
        embed.setColor(UiConstants.COLOR_ERROR);
        return embed.build();
    }

    public MessageEmbed paginatedBanList(PaginationData<Ban> paginationData) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(paginationData.title());

        if (paginationData.items().isEmpty()) {
            embed.setDescription("У данного игрока нет блокировок.");
            embed.setColor(UiConstants.COLOR_SUCCESS);
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
        embed.setColor(UiConstants.COLOR_INFO);
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

        embed.setColor(UiConstants.COLOR_INFO);
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
}
