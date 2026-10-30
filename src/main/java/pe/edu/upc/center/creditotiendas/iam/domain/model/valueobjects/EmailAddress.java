package pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email address used as the unique login identifier of a user.
 * Normalized to lower case and trimmed, so {@code Ana@Mail.com} and {@code ana@mail.com}
 * are the same account.
 */
public record EmailAddress(String value) {

    public static final int MAX_LENGTH = 100;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public EmailAddress {
        if (Objects.isNull(value) || value.isBlank())
            throw new IllegalArgumentException("[EmailAddress] value cannot be blank");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH)
            throw new IllegalArgumentException("[EmailAddress] value cannot exceed %d characters".formatted(MAX_LENGTH));
        if (!EMAIL_PATTERN.matcher(value).matches())
            throw new IllegalArgumentException("[EmailAddress] value has an invalid format");
    }
}
