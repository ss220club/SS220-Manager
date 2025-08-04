package club.ss220.manager.app.util;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class Senders {

    @Nullable
    private final Long dispatchChannelId;

    @Autowired(required = false)
    public Senders(@Nullable @Value("${logging.discord.dispatchChannelId}") Long dispatchChannelId) {
        this.dispatchChannelId = dispatchChannelId;
    }

    public Senders() {
        this.dispatchChannelId = null;
    }

    public Consumer<MessageEmbed> sendEmbed(InteractionHook hook) {
        return messageEmbed -> sendEmbed(hook, messageEmbed);
    }

    public void sendEmbed(InteractionHook hook, MessageEmbed messageEmbed) {
        hook.sendMessageEmbeds(messageEmbed).setAllowedMentions(Collections.emptyList()).queue();
    }

    public Consumer<MessageEmbed> sendEmbedEphemeral(InteractionHook hook) {
        return messageEmbed -> sendEmbedEphemeral(hook, messageEmbed);
    }

    public void sendEmbedEphemeral(InteractionHook hook, MessageEmbed messageEmbed) {
        hook.setEphemeral(true).sendMessageEmbeds(messageEmbed).queue();
    }

    public Consumer<MessageEmbed> sendEmbedMentions(InteractionHook hook) {
        return messageEmbed -> sendEmbedMentions(hook, messageEmbed);
    }

    public void sendEmbedMentions(InteractionHook hook, MessageEmbed messageEmbed) {
        hook.sendMessageEmbeds(messageEmbed).setAllowedMentions(List.of(
                Message.MentionType.ROLE,
                Message.MentionType.CHANNEL,
                Message.MentionType.USER
        )).queue();
    }

    public Consumer<String> sendMessage(InteractionHook hook) {
        return message -> sendMessage(hook, message);
    }

    public void sendMessage(InteractionHook hook, String message) {
        hook.sendMessage(message).setAllowedMentions(Collections.emptyList()).queue();
    }

    public Consumer<String> sendMessageEphemeral(InteractionHook hook) {
        return message -> sendMessageEphemeral(hook, message);
    }

    public void sendMessageEphemeral(InteractionHook hook, String message) {
        hook.setEphemeral(true).sendMessage(message).queue();
    }

    public Consumer<String> sendMessageMentions(InteractionHook hook) {
        return message -> sendMessageMentions(hook, message);
    }

    public void sendMessageMentions(InteractionHook hook, String message) {
        hook.sendMessage(message).setAllowedMentions(List.of(
                Message.MentionType.ROLE,
                Message.MentionType.CHANNEL,
                Message.MentionType.USER
        )).queue();
    }

    public void sendUncaughtExceptionReport(JDA jda, MessageEmbed messageEmbed, String stacktrace) {
        if (dispatchChannelId == null) {
            log.warn("Error dispatch channel is not configured! Error report will not be sent");
            return;
        }

        TextChannel textChannelById = jda.getTextChannelById(dispatchChannelId);
        if (textChannelById == null) {
            log.error("Error dispatch channel is not found! Error report will not be sent");
            return;
        }

        String fileName = "stacktrace_" + LocalDateTime.now() + ".txt";
        try (FileUpload stacktraceFile = FileUpload.fromData(stacktrace.getBytes(), fileName)) {
            textChannelById.sendMessageEmbeds(messageEmbed)
                    .addFiles(stacktraceFile)
                    .setAllowedMentions(Collections.emptyList())
                    .queue();

            log.debug("Sent error report");
        } catch (Exception e) {
            log.error("Error sending error report", e);
        }
    }
}
