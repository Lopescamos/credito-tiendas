package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RequestedItem;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.SaleModalities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Format rules shared by the Credit Management commands. Violations throw
 * {@link IllegalArgumentException}, which the REST layer answers with 400.
 */
final class CreditCommandRules {

    static final int MAX_TERM_DAYS = 3600;
    static final int MAX_INSTALLMENTS = 120;

    private CreditCommandRules() {
    }

    static Long requireId(Long id, String field) {
        if (Objects.isNull(id) || id <= 0)
            throw new IllegalArgumentException("[%s] must be a positive number".formatted(field));
        return id;
    }

    static <T> T requireNonNull(T value, String field) {
        if (Objects.isNull(value))
            throw new IllegalArgumentException("[%s] cannot be null".formatted(field));
        return value;
    }

    static BigDecimal requirePositiveAmount(BigDecimal amount, String field) {
        if (Objects.isNull(amount) || amount.signum() <= 0)
            throw new IllegalArgumentException("[%s] must be greater than zero".formatted(field));
        var normalized = amount.stripTrailingZeros();
        if (normalized.scale() > 2)
            throw new IllegalArgumentException("[%s] cannot have more than 2 decimals".formatted(field));
        if (normalized.precision() - normalized.scale() > 10)
            throw new IllegalArgumentException("[%s] cannot exceed 10 integer digits".formatted(field));
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    static Integer requireRange(Integer value, String field, int min, int max) {
        if (Objects.isNull(value) || value < min || value > max)
            throw new IllegalArgumentException("[%s] must be between %d and %d".formatted(field, min, max));
        return value;
    }

    /**
     * Single payment is exactly one installment; installments means two or more.
     */
    static void requireInstallmentsMatchModality(SaleModalities modality, int installmentCount) {
        if (modality == SaleModalities.SINGLE_PAYMENT && installmentCount != 1)
            throw new IllegalArgumentException("[installmentCount] must be 1 for SINGLE_PAYMENT");
        if (modality == SaleModalities.INSTALLMENTS && installmentCount < 2)
            throw new IllegalArgumentException("[installmentCount] must be at least 2 for INSTALLMENTS");
    }

    static List<RequestedItem> requireItems(List<RequestedItem> items) {
        if (Objects.isNull(items) || items.isEmpty())
            throw new IllegalArgumentException("[items] must contain at least one product");
        var productIds = new HashSet<Long>();
        for (var item : items) {
            requireNonNull(item, "items");
            if (!productIds.add(item.productId()))
                throw new IllegalArgumentException("[items] product %d appears more than once".formatted(item.productId()));
        }
        return List.copyOf(items);
    }

    static String requireText(String value, String field, int maxLength) {
        if (Objects.isNull(value) || value.isBlank())
            throw new IllegalArgumentException("[%s] cannot be blank".formatted(field));
        var trimmed = value.trim();
        if (trimmed.length() > maxLength)
            throw new IllegalArgumentException("[%s] cannot exceed %d characters".formatted(field, maxLength));
        return trimmed;
    }
}
