package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountStatementDetailResource(
        Long id,
        String movementType,
        String description,
        BigDecimal amount,
        LocalDate date) {
}
