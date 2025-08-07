package club.ss220.manager;

import dev.freya02.jda.emojis.unicode.Emojis;
import io.github.freya022.botcommands.api.core.JDAService;
import io.github.freya022.botcommands.api.core.config.JDAConfiguration;
import io.github.freya022.botcommands.api.core.events.BReadyEvent;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@EnableScheduling
@SpringBootApplication(scanBasePackages = "club.ss220")
public class ManagerApplication extends JDAService {

    private final ResourceLoader resourceLoader;
    private final JDAConfiguration jdaConfig;
    private final String token;
    private final String profileName;
    private final Icon profileAvatar;

    public ManagerApplication(ResourceLoader resourceLoader, JDAConfiguration jdaConfig,
                              @Value("${application.token}") String token,
                              @Value("${application.profile.name}") String profileName,
                              @Value("${application.profile.avatar}") String profileAvatarUri) {
        this.resourceLoader = resourceLoader;
        this.jdaConfig = jdaConfig;
        this.token = token;
        this.profileName = profileName;
        this.profileAvatar = loadIcon(profileAvatarUri);
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
        createDefault(token)
                .setActivity(Activity.customStatus("Work in progress " + Emojis.CONSTRUCTION.getFormatted()))
                .addEventListeners(new OnReadyListener())
                .build();
    }

    private Icon loadIcon(String path) {
        try (InputStream inputStream = resourceLoader.getResource(path).getInputStream()) {
            return Icon.from(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

    private class OnReadyListener extends ListenerAdapter {

        @Override
        public void onReady(@NotNull ReadyEvent event) {
            JDA jda = event.getJDA();

            jda.getSelfUser().getManager().setName(profileName).setAvatar(profileAvatar).queue(
                    _ -> log.info("Bot profile updated."),
                    e -> log.error("Error updating bot profile", e)
            );
            jda.getGuilds().forEach(guild -> guild.modifyNickname(guild.getSelfMember(), profileName).queue());

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
