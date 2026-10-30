package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

import java.util.Objects;

/**
 * Activates or deactivates a user. A deactivated user cannot sign in, and tokens already
 * issued stop working on their next request.
 */
public record UpdateUserStatusCommand(Long userId, Boolean active) {
    public UpdateUserStatusCommand {
        userId = UserCommandRules.requireId(userId, "userId");
        if (Objects.isNull(active))
            throw new IllegalArgumentException("[active] cannot be null");
    }
}
