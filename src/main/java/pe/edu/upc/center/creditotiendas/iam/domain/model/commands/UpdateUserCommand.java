package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

/**
 * Updates the personal information of a user. Email and role are not editable.
 */
public record UpdateUserCommand(Long userId, String firstName, String lastName) {
    public UpdateUserCommand {
        userId = UserCommandRules.requireId(userId, "userId");
        firstName = UserCommandRules.requireName(firstName, "firstName");
        lastName = UserCommandRules.requireName(lastName, "lastName");
    }
}
