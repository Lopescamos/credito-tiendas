package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InstallmentDue;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentAllocation;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentComponents;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Applies a payment to the installments being paid (US-05, US-10).
 *
 * <ol>
 *   <li>Mandatory prelation: first every late fee, then every compensatory interest, then every capital;
 *       inside each component, oldest installment first.</li>
 *   <li>No partial payments: the payment must cover exactly everything owed for those installments. A payment
 *       one cent short or one cent over is rejected, so no installment ever stays half paid.</li>
 * </ol>
 *
 * <p>Which installments a payment must cover ("pago íntegro": one installment or everything due) is still an
 * open decision; this policy works with whatever set the caller selects. Prototyped in Credit Management as pure
 * domain logic; Billing uses it to build {@code Imputacion_Pago}.</p>
 */
public final class PaymentAllocationPolicy {

    private PaymentAllocationPolicy() {
    }

    /**
     * @return the allocations in prelation order; components with nothing owed are skipped
     * @throws IllegalArgumentException if the payment is not exactly the amount owed
     */
    public static List<PaymentAllocation> allocate(List<InstallmentDue> dues, BigDecimal payment) {
        if (Objects.isNull(dues) || dues.isEmpty())
            throw new IllegalArgumentException("[PaymentAllocationPolicy] there must be at least one installment to pay");
        if (Objects.isNull(payment) || payment.signum() <= 0)
            throw new IllegalArgumentException("[PaymentAllocationPolicy] payment must be greater than zero");

        var owed = totalOwed(dues);
        if (payment.compareTo(owed) != 0)
            throw new IllegalArgumentException(
                    "[PaymentAllocationPolicy] partial payments are not allowed: payment %s must be exactly %s"
                            .formatted(payment, owed));

        var oldestFirst = dues.stream().sorted(Comparator.comparingInt(InstallmentDue::installmentNumber)).toList();
        var allocations = new ArrayList<PaymentAllocation>();
        var remaining = payment;
        for (var component : PaymentComponents.values()) {
            for (var due : oldestFirst) {
                var amount = due.amountOf(component);
                if (amount.signum() == 0) continue;
                allocations.add(new PaymentAllocation(due.installmentNumber(), component, amount));
                remaining = remaining.subtract(amount);
            }
        }
        if (remaining.signum() != 0)
            throw new IllegalStateException("[PaymentAllocationPolicy] payment was not fully allocated");
        return allocations;
    }

    public static BigDecimal totalOwed(List<InstallmentDue> dues) {
        return dues.stream().map(InstallmentDue::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
