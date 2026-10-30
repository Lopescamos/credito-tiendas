package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;

@Converter(autoApply = true)
public class EmailAddressPersistenceConverter implements AttributeConverter<EmailAddress, String> {
    @Override
    public String convertToDatabaseColumn(EmailAddress emailAddress) {
        return emailAddress == null ? null : emailAddress.value();
    }

    @Override
    public EmailAddress convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new EmailAddress(dbData);
    }
}
