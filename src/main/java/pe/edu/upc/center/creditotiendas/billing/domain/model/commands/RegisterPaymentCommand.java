package pe.edu.upc.center.creditotiendas.billing.domain.model.commands;

import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.PaymentMethods;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Registers a payment on one or several installments of the same credit profile (US-05, US-10). The amount
 * must exactly cover the late fee + interest + capital of every selected installment (no partial payments).
 */
public record RegisterPaymentCommand(Long creditProfileId, List<Long> installmentIds, LocalDate paymentDate,
                                     PaymentMethods paymentMethod, String reference) {
    public RegisterPaymentCommand {
        if (Objects.isNull(creditProfileId) || creditProfileId <= 0)
            throw new IllegalArgumentException("[creditProfileId] must be a positive number");
        if (Objects.isNull(installmentIds) || installmentIds.isEmpty())
            throw new IllegalArgumentException("[installmentIds] must contain at least one installment");
        var seen = new HashSet<Long>();
        for (var id : installmentIds) {
            if (Objects.isNull(id) || id <= 0)
                throw new IllegalArgumentException("[installmentIds] must be positive numbers");
            if (!seen.add(id))
                throw new IllegalArgumentException("[installmentIds] installment %d appears more than once".formatted(id));
        }
        installmentIds = List.copyOf(installmentIds);
        if (Objects.isNull(paymentDate))
            throw new IllegalArgumentException("[paymentDate] cannot be null");
        if (Objects.isNull(paymentMethod))
            throw new IllegalArgumentException("[paymentMethod] cannot be null");
        if (!Objects.isNull(reference)) {
            reference = reference.trim();
            if (reference.length() > 100)
                throw new IllegalArgumentException("[reference] cannot exceed 100 characters");
            if (reference.isBlank()) reference = null;
        }
    }
}
