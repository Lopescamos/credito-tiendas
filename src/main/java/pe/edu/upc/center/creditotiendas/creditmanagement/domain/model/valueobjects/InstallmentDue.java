package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * What is owed today for one installment, split by component.
 */
public record InstallmentDue(int installmentNumber, BigDecimal lateFee, BigDecimal interest, BigDecimal capital) {

    public InstallmentDue {
        if (installmentNumber < 1)
            throw new IllegalArgumentException("[InstallmentDue] installment number must be positive");
        requireNonNegative(lateFee, "lateFee");
        requireNonNegative(interest, "interest");
        requireNonNegative(capital, "capital");
    }

    public BigDecimal amountOf(PaymentComponents component) {
        return switch (component) {
            case LATE_FEE -> lateFee;
            case INTEREST -> interest;
            case CAPITAL -> capital;
        };
    }

    public BigDecimal total() {
        return lateFee.add(interest).add(capital);
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (Objects.isNull(value) || value.signum() < 0)
            throw new IllegalArgumentException("[InstallmentDue] %s cannot be null or negative".formatted(field));
    }
}
