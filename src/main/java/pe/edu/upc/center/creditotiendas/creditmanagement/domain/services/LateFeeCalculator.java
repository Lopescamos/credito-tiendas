package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Late fee (mora) of one overdue installment:
 * {@code lateFee = overdueBalance * [(1 + TEDlate)^daysLate - 1]}.
 *
 * <ul>
 *   <li>{@code overdueBalance} is the unpaid amount of that installment only (interest + capital). Late fees
 *       already charged are never part of the base, and each installment has its own base (US-05).</li>
 *   <li>{@code daysLate} counts calendar days after the due date. Paying on the due date itself is on time:
 *       no late fee.</li>
 *   <li>The fee always runs from the due date with daily compounding, so recomputing it on a later day
 *       replaces the previous amount instead of adding to it, and gives the same result as compounding
 *       step by step.</li>
 * </ul>
 *
 * <p>Prototyped in Credit Management as pure domain logic; Billing uses it to build {@code Cargo_Mora}.</p>
 */
public final class LateFeeCalculator {

    private static final int MONEY_SCALE = 2;
    private static final int FACTOR_SCALE = 12;

    private LateFeeCalculator() {
    }

    public static int daysLate(LocalDate dueDate, LocalDate paymentDate) {
        Objects.requireNonNull(dueDate, "[LateFeeCalculator] due date cannot be null");
        Objects.requireNonNull(paymentDate, "[LateFeeCalculator] payment date cannot be null");
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, paymentDate));
    }

    public static BigDecimal calculate(BigDecimal overdueBalance, LateInterestRate lateRate,
                                       LocalDate dueDate, LocalDate paymentDate) {
        if (Objects.isNull(overdueBalance) || overdueBalance.signum() < 0)
            throw new IllegalArgumentException("[LateFeeCalculator] overdue balance cannot be null or negative");
        Objects.requireNonNull(lateRate, "[LateFeeCalculator] late rate cannot be null");

        int days = daysLate(dueDate, paymentDate);
        if (days == 0 || overdueBalance.signum() == 0)
            return BigDecimal.ZERO.setScale(MONEY_SCALE);

        var growth = BigDecimal.valueOf(Math.pow(1 + lateRate.effectiveDailyRate(), days) - 1)
                .setScale(FACTOR_SCALE, RoundingMode.HALF_UP);
        return overdueBalance.multiply(growth).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
