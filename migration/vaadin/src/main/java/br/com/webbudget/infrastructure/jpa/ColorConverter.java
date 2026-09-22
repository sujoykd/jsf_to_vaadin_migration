package br.com.webbudget.infrastructure.jpa;

import br.com.webbudget.application.components.dto.Color;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ColorConverter implements AttributeConverter<Color, String> {

    @Override
    public String convertToDatabaseColumn(Color attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public Color convertToEntityAttribute(String dbData) {
        return dbData != null ? Color.parse(dbData) : null;
    }
}
