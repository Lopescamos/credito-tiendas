package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreateCreditPurchaseRequestResource;

public class CreateCreditPurchaseRequestCommandFromResourceAssembler {
    public static CreateCreditPurchaseRequestCommand toCommandFromResource(Long creditProfileId,
                                                                           CreateCreditPurchaseRequestResource resource) {
        return new CreateCreditPurchaseRequestCommand(
                creditProfileId, RequestedItemsFromResourceAssembler.toValueObjectsFromResources(resource.items()));
    }
}
