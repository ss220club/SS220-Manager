package club.ss220.manager.config.converters;

import club.ss220.manager.model.GameBuild;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GameBuildConverter implements Converter<String, GameBuild> {

    @Override
    public GameBuild convert(@NotNull String source) {
        return GameBuild.fromName(source);
    }
}
