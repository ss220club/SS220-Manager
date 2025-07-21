package club.ss220.manager.bot.util;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class Senders {

    public Consumer<MessageEmbed> sendEmbed(IDeferrableCallback callback) {
        return messageEmbed -> callback.getHook().sendMessageEmbeds(messageEmbed).queue();
    }

    public Consumer<String> sendMessage(IDeferrableCallback callback) {
        return message -> callback.getHook().sendMessage(message).queue();
    }
}
