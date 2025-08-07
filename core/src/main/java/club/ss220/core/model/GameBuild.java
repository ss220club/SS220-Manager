package club.ss220.core.model;

import club.ss220.core.config.GameConfig;
import club.ss220.core.model.exception.UnknownGameBuildException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum GameBuild {
    PARADISE(GameConfig.BUILD_PARADISE, "Paradise"),
    BANDASTATION(GameConfig.BUILD_BANDASTRATION, "BandaStation");

    private final String qualifier;
    private final String name;

    GameBuild(String qualifier, String name) {
        this.qualifier = qualifier;
        this.name = name;
    }

    public static GameBuild fromName(String name) {
        return Arrays.stream(GameBuild.values())
                     .filter(build -> build.name.equalsIgnoreCase(name))
                     .findFirst()
                     .orElseThrow(() -> new UnknownGameBuildException(name));
    }
}
