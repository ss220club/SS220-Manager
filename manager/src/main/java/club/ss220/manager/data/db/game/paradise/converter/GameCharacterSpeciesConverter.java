package club.ss220.manager.data.db.game.paradise.converter;

import club.ss220.manager.model.GameCharacter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GameCharacterSpeciesConverter implements AttributeConverter<GameCharacter.Species, String> {

    @Override
    public String convertToDatabaseColumn(GameCharacter.Species species) {
        return species.getName();
    }

    @Override
    public GameCharacter.Species convertToEntityAttribute(String dbData) {
        return GameCharacter.Species.fromName(dbData);
    }
}
