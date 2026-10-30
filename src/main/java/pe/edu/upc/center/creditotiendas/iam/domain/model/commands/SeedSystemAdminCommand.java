package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

/**
 * Creates the initial system admin at startup when none exists. Without it nobody could
 * create stores or users, because there is no public sign-up.
 */
public record SeedSystemAdminCommand(String email, String password, String firstName, String lastName) {
    public SeedSystemAdminCommand {
        email = UserCommandRules.requireEmail(email);
        password = UserCommandRules.requirePassword(password);
        firstName = UserCommandRules.requireName(firstName, "firstName");
        lastName = UserCommandRules.requireName(lastName, "lastName");
    }
}
