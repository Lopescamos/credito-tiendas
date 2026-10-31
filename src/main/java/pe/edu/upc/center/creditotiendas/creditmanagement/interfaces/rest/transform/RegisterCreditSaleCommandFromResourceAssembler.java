package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.RegisterCreditSaleCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleRequestResource;

public class RegisterCreditSaleCommandFromResourceAssembler {
    public static RegisterCreditSaleCommand toCommandFromResource(Long creditProfileId, CreditSaleRequestResource resource) {
        return new RegisterCreditSaleCommand(
                creditProfileId, resource.modality(), resource.installmentCount(), resource.graceDays(),
                RequestedItemsFromResourceAssembler.toValueObjectsFromResources(resource.items()));
    }
}
