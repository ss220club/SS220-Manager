package club.ss220.manager.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Data
@Builder
public class GameServer {
    private final String name;
    private final Build build;
    private final String host;
    private final int port;
    private final String key;

    @Getter
    public enum Build {
        PARADISE("Paradise"),
        BANDASTATION("BandaStation");

        private final String name;

        Build(String name) {
            this.name = name;
        }

        public static Build fromName(String name) {
            return Arrays.stream(Build.values())
                    .filter(build -> build.name.equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Unknown game build: " + name));
        }
    }

    public String getFullName() {
        return build.name + " " + name;
    }
}
