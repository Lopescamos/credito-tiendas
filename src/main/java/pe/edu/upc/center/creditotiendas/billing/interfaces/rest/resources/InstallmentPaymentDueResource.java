package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Breakdown of what an installment owes today (US-05 desglose exigible, US-10 total exigible).
 */
public record InstallmentPaymentDueResource(
        Long installmentId,
        int number,
        LocalDate dueDate,
        int daysLate,
        BigDecimal lateFee,
        BigDecimal interest,
        BigDecimal capital,
        BigDecimal total) {
}
