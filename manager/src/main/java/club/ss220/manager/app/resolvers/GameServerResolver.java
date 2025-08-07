package club.ss220.manager.app.resolvers;

import club.ss220.core.config.GameConfig;
import club.ss220.core.model.GameServer;
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Resolver
public class GameServerResolver
        extends ClassParameterResolver<GameServerResolver, GameServer>
        implements SlashParameterResolver<GameServerResolver, GameServer> {

    private final GameConfig gameConfig;

    public GameServerResolver(GameConfig gameConfig) {
        super(GameServer.class);
        this.gameConfig = gameConfig;
    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

    @NotNull
    @Override
    public Collection<Command.Choice> getPredefinedChoices(@Nullable Guild guild) {
        return gameConfig.getServers().stream()
                .map(server -> new Command.Choice(server.getFullName(), server.getName()))
                .toList();
    }

    @Nullable
    @Override
    public GameServer resolve(@NotNull SlashCommandOption option,
                              @NotNull CommandInteractionPayload event,
                              @NotNull OptionMapping optionMapping) {
        return gameConfig.getServerByName(optionMapping.getAsString()).orElse(null);
    }
}
