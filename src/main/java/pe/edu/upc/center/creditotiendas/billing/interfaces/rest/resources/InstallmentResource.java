package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One installment of a credit sale with its real state (US-07).
 */
public record InstallmentResource(
        Long id,
        Long creditSaleId,
        int number,
        LocalDate dueDate,
        BigDecimal capital,
        BigDecimal interest,
        BigDecimal fixedInstallment,
        BigDecimal remainingBalance,
        String status,
        LocalDate paymentDate) {
}
