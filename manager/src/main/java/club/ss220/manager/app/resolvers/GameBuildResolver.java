package club.ss220.manager.app.resolvers;

import club.ss220.core.model.GameBuild;
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

import java.util.Arrays;
import java.util.Collection;

@Resolver
public class GameBuildResolver
        extends ClassParameterResolver<GameBuildResolver, GameBuild>
        implements SlashParameterResolver<GameBuildResolver, GameBuild> {

    public GameBuildResolver() {
        super(GameBuild.class);
    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

    @NotNull
    @Override
    public Collection<Command.Choice> getPredefinedChoices(@Nullable Guild guild) {
        return Arrays.stream(GameBuild.values())
                .map(build -> new Command.Choice(build.getName(), build.getName()))
                .toList();
    }

    @Nullable
    @Override
    public GameBuild resolve(@NotNull SlashCommandOption option,
                             @NotNull CommandInteractionPayload event,
                             @NotNull OptionMapping optionMapping) {
        return GameBuild.fromName(optionMapping.getAsString());
    }
}
