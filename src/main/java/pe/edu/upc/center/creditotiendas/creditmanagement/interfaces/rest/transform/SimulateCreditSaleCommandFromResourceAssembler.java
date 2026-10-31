package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.SimulateCreditSaleCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleRequestResource;

public class SimulateCreditSaleCommandFromResourceAssembler {
    public static SimulateCreditSaleCommand toCommandFromResource(Long creditProfileId, CreditSaleRequestResource resource) {
        return new SimulateCreditSaleCommand(
                creditProfileId, resource.modality(), resource.installmentCount(), resource.graceDays(),
                RequestedItemsFromResourceAssembler.toValueObjectsFromResources(resource.items()));
    }
}
