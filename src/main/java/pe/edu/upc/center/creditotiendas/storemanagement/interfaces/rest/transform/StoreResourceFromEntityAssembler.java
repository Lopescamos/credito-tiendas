package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.StoreResource;

public class StoreResourceFromEntityAssembler {
    public static StoreResource toResourceFromEntity(Store store) {
        return new StoreResource(
                store.getId(), store.getRuc().value(), store.getLegalName(), store.getTradeName(),
                store.getAddress(), store.getPhone(), store.isActive());
    }
}
