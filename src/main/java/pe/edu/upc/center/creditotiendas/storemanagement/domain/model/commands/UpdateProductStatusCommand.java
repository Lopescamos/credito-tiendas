package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.*;

/**
 * Activates or deactivates a catalog item. Deactivating replaces deleting (US-11).
 */
public record UpdateProductStatusCommand(Long productId, Boolean active) {
    public UpdateProductStatusCommand {
        productId = requireId(productId, "productId");
        active = requireFlag(active, "active");
    }
}
