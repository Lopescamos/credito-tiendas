package pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What a single installment owes at a given moment, split by component (US-05/US-10 breakdown).
 */
public record InstallmentPaymentDue(
        Long installmentId,
        int number,
        LocalDate dueDate,
        int daysLate,
        BigDecimal lateFee,
        BigDecimal interest,
        BigDecimal capital,
        BigDecimal total) {
}
