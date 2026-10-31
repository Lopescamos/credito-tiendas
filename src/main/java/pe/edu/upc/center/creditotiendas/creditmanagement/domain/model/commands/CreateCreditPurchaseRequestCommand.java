package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RequestedItem;

import java.util.List;

import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreditCommandRules.*;

/**
 * A customer asks their store to sell them catalog items on credit (US-08).
 */
public record CreateCreditPurchaseRequestCommand(Long creditProfileId, List<RequestedItem> items) {
    public CreateCreditPurchaseRequestCommand {
        creditProfileId = requireId(creditProfileId, "creditProfileId");
        items = requireItems(items);
    }
}
