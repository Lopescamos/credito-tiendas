package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Format rules shared by the Store Management commands. Violations throw
 * {@link IllegalArgumentException}, which the REST layer answers with 400.
 */
final class StoreManagementCommandRules {

    /** Amounts are stored as DECIMAL(12,2): up to 10 integer digits and 2 decimals. */
    private static final int AMOUNT_MAX_INTEGER_DIGITS = 10;
    private static final int AMOUNT_MAX_DECIMALS = 2;

    private StoreManagementCommandRules() {
    }

    static String requireText(String value, String field, int maxLength) {
        if (Objects.isNull(value) || value.isBlank())
            throw new IllegalArgumentException("[%s] cannot be blank".formatted(field));
        return checkLength(value.trim(), field, maxLength);
    }

    /**
     * @return the trimmed value, or {@code null} when blank
     */
    static String optionalText(String value, String field, int maxLength) {
        if (Objects.isNull(value) || value.isBlank()) return null;
        return checkLength(value.trim(), field, maxLength);
    }

    static Long requireId(Long id, String field) {
        if (Objects.isNull(id) || id <= 0)
            throw new IllegalArgumentException("[%s] must be a positive number".formatted(field));
        return id;
    }

    static Long optionalId(Long id, String field) {
        return Objects.isNull(id) ? null : requireId(id, field);
    }

    static BigDecimal requirePositiveAmount(BigDecimal amount, String field) {
        if (Objects.isNull(amount) || amount.signum() <= 0)
            throw new IllegalArgumentException("[%s] must be greater than zero".formatted(field));
        var normalized = amount.stripTrailingZeros();
        if (normalized.scale() > AMOUNT_MAX_DECIMALS)
            throw new IllegalArgumentException("[%s] cannot have more than %d decimals".formatted(field, AMOUNT_MAX_DECIMALS));
        if (normalized.precision() - normalized.scale() > AMOUNT_MAX_INTEGER_DIGITS)
            throw new IllegalArgumentException("[%s] cannot exceed %d integer digits".formatted(field, AMOUNT_MAX_INTEGER_DIGITS));
        return amount.setScale(AMOUNT_MAX_DECIMALS);
    }

    static Boolean requireFlag(Boolean value, String field) {
        if (Objects.isNull(value))
            throw new IllegalArgumentException("[%s] cannot be null".formatted(field));
        return value;
    }

    private static String checkLength(String value, String field, int maxLength) {
        if (value.length() > maxLength)
            throw new IllegalArgumentException("[%s] cannot exceed %d characters".formatted(field, maxLength));
        return value;
    }
}
