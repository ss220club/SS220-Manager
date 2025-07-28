package club.ss220.manager.app.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Component
public class Senders {

    public Consumer<MessageEmbed> sendEmbed(InteractionHook hook) {
        return messageEmbed -> sendEmbed(hook, messageEmbed);
    }

    public void sendEmbed(InteractionHook hook, MessageEmbed messageEmbed) {
        hook.sendMessageEmbeds(messageEmbed).queue();
    }

    public Consumer<String> sendMessage(InteractionHook hook) {
        return message -> sendMessage(hook, message);
    }

    public void sendMessage(InteractionHook hook, String message) {
        hook.sendMessage(message).setAllowedMentions(Collections.emptyList()).queue();
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
}
