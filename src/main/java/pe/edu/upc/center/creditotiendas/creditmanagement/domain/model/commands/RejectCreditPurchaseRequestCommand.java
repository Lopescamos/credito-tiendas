package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * Rejects a pending request. The reason is mandatory and shown to the customer (US-12, US-09).
 */
public record RejectCreditPurchaseRequestCommand(Long purchaseRequestId, String reason) {
    public RejectCreditPurchaseRequestCommand {
        purchaseRequestId = requireId(purchaseRequestId, "purchaseRequestId");
        reason = requireText(reason, "reason", 300);
    }
}
