package club.ss220.manager.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@RequiredArgsConstructor
public class User {

    @NotNull
    protected final Integer id;
    @NotNull
    protected final String ckey;
    @NotNull
    protected final Long discordId;
}
