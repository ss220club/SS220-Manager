package club.ss220.manager.bot;

import club.ss220.manager.config.BotConfig;
import io.github.freya022.botcommands.api.core.JDAService;
import io.github.freya022.botcommands.api.core.config.JDAConfiguration;
import io.github.freya022.botcommands.api.core.events.BReadyEvent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class Bot extends JDAService {

    private final JDAConfiguration jdaConfig;
    private final BotConfig botConfig;

    @Autowired
    public Bot(JDAConfiguration jdaConfig, BotConfig botConfig) {
        this.jdaConfig = jdaConfig;
        this.botConfig = botConfig;
    }

    @NotNull
    @Override
    public Set<CacheFlag> getCacheFlags() {
        return jdaConfig.getCacheFlags();
    }

    @NotNull
    @Override
    public Set<GatewayIntent> getIntents() {
        return jdaConfig.getIntents();
    }

    @Override
    protected void createJDA(@NotNull BReadyEvent bReadyEvent, @NotNull IEventManager iEventManager) {
        createDefault(botConfig.getToken())
                .setActivity(Activity.customStatus("Work in progress 🚧"))
                .addEventListeners(new OnReadyListener())
                .build();
    }

    private static class OnReadyListener extends ListenerAdapter {

        @Override
        public void onReady(@NotNull ReadyEvent event) {
            JDA jda = event.getJDA();
            List<Command> globalCommands = jda.retrieveCommands().complete();
            List<Command> guildCommands = jda
                    .getGuilds()
                    .stream()
                    .flatMap(guild -> guild.retrieveCommands().complete().stream())
                    .toList();
            log.info("Bot started with {} global commands and {} guild commands registered.",
                     globalCommands.size(), guildCommands.size());
        }
    }
}
