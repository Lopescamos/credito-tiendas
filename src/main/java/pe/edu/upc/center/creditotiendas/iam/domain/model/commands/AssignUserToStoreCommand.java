package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

/**
 * Links a store admin to a store (US-14, table {@code user_stores}).
 */
public record AssignUserToStoreCommand(Long userId, Long storeId) {
    public AssignUserToStoreCommand {
        userId = UserCommandRules.requireId(userId, "userId");
        storeId = UserCommandRules.requireId(storeId, "storeId");
    }
}
