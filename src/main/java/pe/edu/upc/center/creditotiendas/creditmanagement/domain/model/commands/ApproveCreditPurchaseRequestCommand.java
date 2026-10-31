package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * Approves a pending request; the credit sale is generated automatically (US-12).
 */
public record ApproveCreditPurchaseRequestCommand(Long purchaseRequestId) {
    public ApproveCreditPurchaseRequestCommand {
        purchaseRequestId = requireId(purchaseRequestId, "purchaseRequestId");
    }
}
