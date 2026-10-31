package pe.edu.upc.center.creditotiendas.billing.domain.model.commands;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.ScheduledInstallment;

import java.util.List;
import java.util.Objects;

/**
 * Internal command fired when Billing receives {@code CreditSaleRegisteredEvent} from Credit. Not exposed
 * through the REST API: no admin registers cuotas manually.
 */
public record CreateInstallmentScheduleCommand(Long creditSaleId, Long creditProfileId,
                                               List<ScheduledInstallment> installments) {
    public CreateInstallmentScheduleCommand {
        if (Objects.isNull(creditSaleId) || creditSaleId <= 0)
            throw new IllegalArgumentException("[creditSaleId] must be a positive number");
        if (Objects.isNull(creditProfileId) || creditProfileId <= 0)
            throw new IllegalArgumentException("[creditProfileId] must be a positive number");
        if (Objects.isNull(installments) || installments.isEmpty())
            throw new IllegalArgumentException("[installments] cannot be empty");
        installments = List.copyOf(installments);
    }
}
