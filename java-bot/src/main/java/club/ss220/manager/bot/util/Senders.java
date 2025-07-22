package club.ss220.manager.bot.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Component
public class Senders {

    public Consumer<MessageEmbed> sendEmbed(IDeferrableCallback callback) {
        return messageEmbed -> sendEmbed(callback, messageEmbed);
    }

    public void sendEmbed(IDeferrableCallback callback, MessageEmbed messageEmbed) {
        callback.getHook().sendMessageEmbeds(messageEmbed).queue();
    }

    public Consumer<String> sendMessage(IDeferrableCallback callback) {
        return message -> sendMessage(callback, message);
    }

    public void sendMessage(IDeferrableCallback callback, String message) {
        MessageCreateData messageData = new MessageCreateBuilder()
                .setContent(message)
                .setAllowedMentions(Collections.emptyList())
                .build();
        callback.getHook().sendMessage(messageData).queue();
    }

    public Consumer<String> sendMessageMentions(IDeferrableCallback callback) {
        return message -> sendMessageMentions(callback, message);
    }

    public void sendMessageMentions(IDeferrableCallback callback, String message) {
        MessageCreateData messageData = new MessageCreateBuilder()
                .setContent(message)
                .setAllowedMentions(List.of(
                        Message.MentionType.ROLE,
                        Message.MentionType.CHANNEL,
                        Message.MentionType.USER
                ))
                .build();
        callback.getHook().sendMessage(messageData).queue();
    }
}
