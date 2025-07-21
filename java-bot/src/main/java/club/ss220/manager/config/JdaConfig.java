package club.ss220.manager.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdaConfig {

    private final BotConfig botConfig;

    @Autowired
    public JdaConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public JDA jda() throws InterruptedException {
        JDA jda = JDABuilder.createDefault(botConfig.getToken())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .enableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.EMOJI
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();

        jda.awaitReady();
        return jda;
    }
}
