package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.List;

/**
 * Result of applying the French method to a credit sale.
 *
 * @param financedCapital {@code C'}: the sale amount plus the interest capitalized during the grace days
 * @param fixedInstallment {@code R}: the installment paid every 30 days
 */
public record PaymentPlan(
        BigDecimal financedCapital,
        BigDecimal fixedInstallment,
        List<ScheduledInstallment> installments) {

    public PaymentPlan {
        installments = List.copyOf(installments);
    }

    public BigDecimal totalInterest() {
        return installments.stream().map(ScheduledInstallment::interest).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalToPay() {
        return installments.stream().map(ScheduledInstallment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
