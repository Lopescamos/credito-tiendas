package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RequestedItem;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.RequestedItemResource;

import java.util.List;

public class RequestedItemsFromResourceAssembler {
    public static List<RequestedItem> toValueObjectsFromResources(List<RequestedItemResource> resources) {
        return resources.stream()
                .map(resource -> new RequestedItem(resource.productId(), resource.quantity()))
                .toList();
    }
}
