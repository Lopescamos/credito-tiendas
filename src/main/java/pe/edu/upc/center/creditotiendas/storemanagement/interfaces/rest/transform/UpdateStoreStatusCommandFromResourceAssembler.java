package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateStoreStatusCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.UpdateStoreStatusResource;

public class UpdateStoreStatusCommandFromResourceAssembler {
    public static UpdateStoreStatusCommand toCommandFromResource(Long storeId, UpdateStoreStatusResource resource) {
        return new UpdateStoreStatusCommand(storeId, resource.active());
    }
}
