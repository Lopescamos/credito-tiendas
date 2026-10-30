package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

import java.util.Objects;

/**
 * Credentials submitted to sign in. The password format is not checked here on purpose:
 * a wrong password must get the same answer as an unknown email.
 */
public record SignInCommand(String email, String password) {
    public SignInCommand {
        email = UserCommandRules.requireEmail(email);
        if (Objects.isNull(password) || password.isEmpty())
            throw new IllegalArgumentException("[password] cannot be empty");
    }
}
