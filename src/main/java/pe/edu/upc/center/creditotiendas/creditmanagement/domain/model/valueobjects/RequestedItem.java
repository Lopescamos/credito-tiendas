package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A catalog item and the quantity the customer takes, before its price is looked up.
 *
 * @param quantity up to 2 decimals (e.g. 1.25 kg), stored as DECIMAL(10,2)
 */
public record RequestedItem(Long productId, BigDecimal quantity) {

    public RequestedItem {
        if (Objects.isNull(productId) || productId <= 0)
            throw new IllegalArgumentException("[RequestedItem] productId must be a positive number");
        if (Objects.isNull(quantity) || quantity.signum() <= 0)
            throw new IllegalArgumentException("[RequestedItem] quantity must be greater than zero");
        var normalized = quantity.stripTrailingZeros();
        if (normalized.scale() > 2)
            throw new IllegalArgumentException("[RequestedItem] quantity cannot have more than 2 decimals");
        if (normalized.precision() - normalized.scale() > 8)
            throw new IllegalArgumentException("[RequestedItem] quantity cannot exceed 8 integer digits");
        quantity = quantity.setScale(2, RoundingMode.UNNECESSARY);
    }
}
