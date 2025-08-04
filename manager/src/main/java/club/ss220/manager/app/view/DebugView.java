package club.ss220.manager.app.view;

import club.ss220.manager.app.util.Senders;
import club.ss220.manager.model.GameServer;
import club.ss220.manager.model.GameServerStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

@Component
public class DebugView {

    private final Senders senders;
    private final ObjectMapper objectMapper;

    public DebugView(Senders senders) {
        this.senders = senders;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    public void renderServerStatus(InteractionHook hook, GameServer server, GameServerStatus serverStatus) {
        MessageEmbed embed = createServerStatusEmbed(server, serverStatus);
        senders.sendEmbed(hook, embed);
    }

    private MessageEmbed createServerStatusEmbed(GameServer server, GameServerStatus serverStatus) {
        return new EmbedBuilder()
                .setTitle("Статус сервера " + server.getFullName())
                .setDescription(createServerStatusBlock(serverStatus))
                .setColor(UiConstants.COLOR_INFO)
                .build();
    }

    private String createServerStatusBlock(GameServerStatus serverStatus) {
        try {
            return "```json\n" + objectMapper.writeValueAsString(serverStatus.getRawData()) + "\n```";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
