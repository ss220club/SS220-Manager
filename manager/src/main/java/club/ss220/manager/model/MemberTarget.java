package club.ss220.manager.model;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

public record MemberTarget(@Nullable User discordUser, @Nullable String query) {

    public static MemberTarget fromUser(User user) {
        return new MemberTarget(user, null);
    }

    public static MemberTarget fromQuery(String query) {
        return new MemberTarget(null, query);
    }

    public String getDisplayString() {
        if (discordUser != null) {
            return discordUser.getAsMention();
        } else {
            return query;
        }
    }
}
