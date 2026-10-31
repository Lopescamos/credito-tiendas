package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduledInstallmentResource(
        int number,
        LocalDate dueDate,
        BigDecimal interest,
        BigDecimal capital,
        BigDecimal amount,
        BigDecimal remainingBalance) {
}
