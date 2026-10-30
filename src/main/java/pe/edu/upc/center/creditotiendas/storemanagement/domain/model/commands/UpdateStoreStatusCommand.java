package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.*;

/**
 * Activates or deactivates a store (US-13).
 */
public record UpdateStoreStatusCommand(Long storeId, Boolean active) {
    public UpdateStoreStatusCommand {
        storeId = requireId(storeId, "storeId");
        active = requireFlag(active, "active");
    }
}
