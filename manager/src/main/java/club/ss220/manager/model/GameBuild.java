package club.ss220.manager.model;

import club.ss220.manager.config.GameConfig;
import lombok.Getter;

import java.util.Arrays;
import java.util.NoSuchElementException;

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
                     .orElseThrow(() -> new NoSuchElementException("Unknown game build: " + name));
    }
}
