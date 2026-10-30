package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;

import java.util.Objects;

/**
 * Creates a store admin or client user (US-14). The password arrives in plain text and is
 * hashed by the command service before building the aggregate.
 */
public record CreateUserCommand(String email, String firstName, String lastName, String password, Roles role) {
    public CreateUserCommand {
        email = UserCommandRules.requireEmail(email);
        firstName = UserCommandRules.requireName(firstName, "firstName");
        lastName = UserCommandRules.requireName(lastName, "lastName");
        password = UserCommandRules.requirePassword(password);
        if (Objects.isNull(role))
            throw new IllegalArgumentException("[role] cannot be null");
    }
}
