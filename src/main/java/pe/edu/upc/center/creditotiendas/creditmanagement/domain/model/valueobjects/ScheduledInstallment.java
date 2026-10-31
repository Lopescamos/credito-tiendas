package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of a payment plan (model: Cuota_Cronograma).
 *
 * @param amount           what the customer pays in this installment (interest + capital); equals the
 *                         fixed installment except in the last one, which absorbs rounding
 * @param remainingBalance capital still owed after paying this installment
 */
public record ScheduledInstallment(
        int number,
        LocalDate dueDate,
        BigDecimal interest,
        BigDecimal capital,
        BigDecimal amount,
        BigDecimal remainingBalance) {
}
