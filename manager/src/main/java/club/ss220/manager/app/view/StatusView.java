package club.ss220.manager.app.view;

import club.ss220.manager.app.util.Formatters;
import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.ApplicationStatus;
import dev.freya02.jda.emojis.unicode.Emojis;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@AllArgsConstructor
public class StatusView {

    private final Senders senders;
    private final Formatters formatters;

    public void renderApplicationStatus(InteractionHook hook, ApplicationStatus applicationStatus) {
        MessageEmbed embed = createApplicationStatusEmbed(applicationStatus);
        senders.sendEmbedEphemeral(hook, embed);
    }

    private MessageEmbed createApplicationStatusEmbed(ApplicationStatus applicationStatus) {
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
        embed.setColor(UiConstants.COLOR_INFO);
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

    private static UnicodeEmoji mapStatusLevel(ApplicationStatus.Level level) {
        return switch (level) {
            case OK -> Emojis.GREEN_CIRCLE;
            case WARNING -> Emojis.ORANGE_CIRCLE;
            case CRITICAL -> Emojis.RED_CIRCLE;
        };
    }
}
