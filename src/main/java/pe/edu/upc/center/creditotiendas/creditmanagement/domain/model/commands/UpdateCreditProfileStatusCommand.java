package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * Activates or deactivates a credit profile (alta/baja in US-04). An inactive profile cannot buy on credit.
 */
public record UpdateCreditProfileStatusCommand(Long creditProfileId, Boolean active) {
    public UpdateCreditProfileStatusCommand {
        creditProfileId = requireId(creditProfileId, "creditProfileId");
        requireNonNull(active, "active");
    }
}
