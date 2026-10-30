package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

/**
 * Deactivates the link between a store admin and a store.
 */
public record UnassignUserFromStoreCommand(Long userId, Long storeId) {
    public UnassignUserFromStoreCommand {
        userId = UserCommandRules.requireId(userId, "userId");
        storeId = UserCommandRules.requireId(storeId, "storeId");
    }
}
