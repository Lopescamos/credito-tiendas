package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateProductStatusCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.UpdateProductStatusResource;

public class UpdateProductStatusCommandFromResourceAssembler {
    public static UpdateProductStatusCommand toCommandFromResource(Long productId, UpdateProductStatusResource resource) {
        return new UpdateProductStatusCommand(productId, resource.active());
    }
}
