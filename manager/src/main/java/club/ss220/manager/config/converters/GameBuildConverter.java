package club.ss220.manager.config.converters;

import club.ss220.manager.model.GameServer;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GameBuildConverter implements Converter<String, GameServer.Build> {

    @Override
    public GameServer.Build convert(@NotNull String source) {
        return GameServer.Build.fromName(source);
    }
}
