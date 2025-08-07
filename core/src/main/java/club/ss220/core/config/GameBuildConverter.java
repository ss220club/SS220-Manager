package club.ss220.core.config;

import club.ss220.core.model.GameBuild;
import jakarta.annotation.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GameBuildConverter implements Converter<String, GameBuild> {

    @Override
    public GameBuild convert(@Nullable String source) {
        return GameBuild.fromName(source);
    }
}
