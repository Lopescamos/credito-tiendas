package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;

@Converter(autoApply = true)
public class RucPersistenceConverter implements AttributeConverter<Ruc, String> {
    @Override
    public String convertToDatabaseColumn(Ruc ruc) {
        return ruc == null ? null : ruc.value();
    }

    @Override
    public Ruc convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Ruc(dbData);
    }
}
