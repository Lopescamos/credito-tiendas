package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.CreateProductResource;

public class CreateProductCommandFromResourceAssembler {
    public static CreateProductCommand toCommandFromResource(Long storeId, CreateProductResource resource) {
        return new CreateProductCommand(
                storeId, resource.name(), resource.description(), resource.type(), resource.supplier(),
                resource.brand(), resource.measurementUnitId(), resource.cashPrice(), resource.creditPrice());
    }
}
