package club.ss220.manager.model;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
