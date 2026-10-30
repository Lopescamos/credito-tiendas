package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.CreateStoreResource;

public class CreateStoreCommandFromResourceAssembler {
    public static CreateStoreCommand toCommandFromResource(CreateStoreResource resource) {
        return new CreateStoreCommand(
                resource.ruc(), resource.legalName(), resource.tradeName(), resource.address(), resource.phone());
    }
}
