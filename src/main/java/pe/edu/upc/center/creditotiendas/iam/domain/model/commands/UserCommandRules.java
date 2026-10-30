package pe.edu.upc.center.creditotiendas.iam.domain.model.commands;

import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Format rules shared by the user commands. Violations throw {@link IllegalArgumentException},
 * which the REST layer answers with 400.
 */
final class UserCommandRules {

    static final int NAME_MAX_LENGTH = 60;
    static final int PASSWORD_MIN_LENGTH = 8;
    /** BCrypt only uses the first 72 bytes of the password. */
    static final int PASSWORD_MAX_BYTES = 72;

    private UserCommandRules() {
    }

    static String requireEmail(String email) {
        return new EmailAddress(email).value();
    }

    static String requireName(String value, String field) {
        if (Objects.isNull(value) || value.isBlank())
            throw new IllegalArgumentException("[%s] cannot be blank".formatted(field));
        var trimmed = value.trim();
        if (trimmed.length() > NAME_MAX_LENGTH)
            throw new IllegalArgumentException("[%s] cannot exceed %d characters".formatted(field, NAME_MAX_LENGTH));
        return trimmed;
    }

    static String requirePassword(String password) {
        if (Objects.isNull(password) || password.length() < PASSWORD_MIN_LENGTH)
            throw new IllegalArgumentException("[password] must have at least %d characters".formatted(PASSWORD_MIN_LENGTH));
        if (password.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX_BYTES)
            throw new IllegalArgumentException("[password] cannot exceed %d bytes".formatted(PASSWORD_MAX_BYTES));
        return password;
    }

    static Long requireId(Long id, String field) {
        if (Objects.isNull(id) || id <= 0)
            throw new IllegalArgumentException("[%s] must be a positive number".formatted(field));
        return id;
    }
}
