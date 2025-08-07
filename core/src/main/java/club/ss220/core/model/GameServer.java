package club.ss220.core.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
public class GameServer {

    @NotNull
    String name;
    @NotNull
    GameBuild build;
    @NotNull
    @ToString.Exclude
    String host;
    @ToString.Exclude
    int port;
    @ToString.Exclude
    @Nullable
    String key;

    public String getFullName() {
        return build.getName() + " " + name;
    }
}
