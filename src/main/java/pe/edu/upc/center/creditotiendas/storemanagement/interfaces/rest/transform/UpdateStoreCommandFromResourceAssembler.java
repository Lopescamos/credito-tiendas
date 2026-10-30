package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateStoreCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.UpdateStoreResource;

public class UpdateStoreCommandFromResourceAssembler {
    public static UpdateStoreCommand toCommandFromResource(Long storeId, UpdateStoreResource resource) {
        return new UpdateStoreCommand(
                storeId, resource.legalName(), resource.tradeName(), resource.address(), resource.phone());
    }
}
