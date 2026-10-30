package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.UpdateProductResource;

public class UpdateProductCommandFromResourceAssembler {
    public static UpdateProductCommand toCommandFromResource(Long productId, UpdateProductResource resource) {
        return new UpdateProductCommand(
                productId, resource.name(), resource.description(), resource.type(), resource.supplier(),
                resource.brand(), resource.measurementUnitId(), resource.cashPrice(), resource.creditPrice());
    }
}
