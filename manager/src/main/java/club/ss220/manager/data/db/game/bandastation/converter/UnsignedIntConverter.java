package club.ss220.manager.data.db.game.bandastation.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UnsignedIntConverter implements AttributeConverter<Long, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Long attribute) {
        return attribute != null ? attribute.intValue() : null;
    }

    @Override
    public Long convertToEntityAttribute(Integer dbData) {
        return dbData != null ? Integer.toUnsignedLong(dbData) : null;
    }
}
