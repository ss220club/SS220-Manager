package club.ss220.manager.app.resolvers;

import club.ss220.manager.model.MemberTarget;
import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandOption;
import io.github.freya022.botcommands.api.core.service.annotations.Resolver;
import io.github.freya022.botcommands.api.parameters.ClassParameterResolver;
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Resolver
public class MemberTargetResolver
        extends ClassParameterResolver<MemberTargetResolver, MemberTarget>
        implements SlashParameterResolver<MemberTargetResolver, MemberTarget> {

    public MemberTargetResolver() {
        super(MemberTarget.class);
    }

    @NotNull
    @Override
    public OptionType getOptionType() {
        return OptionType.STRING;
    }

    @Nullable
    @Override
    public MemberTarget resolve(@NotNull SlashCommandOption option,
                                @NotNull CommandInteractionPayload event,
                                @NotNull OptionMapping optionMapping) {
        String query = optionMapping.getAsString();
        return MemberTarget.fromQuery(query);
    }
}
