package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects;

import java.util.Objects;

/**
 * Peruvian taxpayer number (RUC) of a store: exactly 11 digits (US-13).
 */
public record Ruc(String value) {
    public Ruc {
        if (Objects.isNull(value) || value.isBlank())
            throw new IllegalArgumentException("[Ruc] value cannot be blank");
        value = value.trim();
        if (!value.matches("\\d{11}"))
            throw new IllegalArgumentException("[Ruc] value must have exactly 11 digits");
    }
}
