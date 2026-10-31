package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/**
 * French-method payment plan: {@code financedCapital} is the sale amount plus capitalized grace interest.
 */
public record PaymentPlanResource(
        BigDecimal financedCapital,
        BigDecimal fixedInstallment,
        BigDecimal totalInterest,
        BigDecimal totalToPay,
        List<ScheduledInstallmentResource> installments) {
}
