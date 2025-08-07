package club.ss220.core.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
