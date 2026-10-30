package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.ProductResource;

public class ProductResourceFromEntityAssembler {
    public static ProductResource toResourceFromEntity(Product product) {
        return new ProductResource(
                product.getId(), product.getStoreId(), product.getName(), product.getDescription(),
                product.getType().name(), product.getSupplier(), product.getBrand(),
                product.getMeasurementUnitId(), product.getImageUrl(),
                product.getCashPrice(), product.getCreditPrice(), product.isActive());
    }
}
