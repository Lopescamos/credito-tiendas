package pe.edu.upc.center.creditotiendas.billing.domain.model.commands;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Scheduler-triggered sweep that flips {@code PENDING} installments to {@code OVERDUE} when the due date has
 * passed and asks Credit to move the sale to {@code OVERDUE} too.
 */
public record MarkOverdueInstallmentsCommand(LocalDate asOf) {
    public MarkOverdueInstallmentsCommand {
        if (Objects.isNull(asOf))
            throw new IllegalArgumentException("[asOf] cannot be null");
    }
}
