package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.RejectCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.RejectCreditPurchaseRequestResource;

public class RejectCreditPurchaseRequestCommandFromResourceAssembler {
    public static RejectCreditPurchaseRequestCommand toCommandFromResource(Long purchaseRequestId,
                                                                           RejectCreditPurchaseRequestResource resource) {
        return new RejectCreditPurchaseRequestCommand(purchaseRequestId, resource.reason());
    }
}
