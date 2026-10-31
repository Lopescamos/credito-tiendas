package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Annual late-payment (moratorium) rate, as a percentage with 7 decimals. Base year of 360 days.
 *
 * <p>A NOMINAL late rate always capitalizes daily. That is a fixed rule of the domain, not a condition each
 * store agrees, so it is the constant {@link #CAPITALIZATION_PERIOD_DAYS} and is not persisted per row (the
 * model has no capitalization column for the late rate).</p>
 *
 * <p>Late fee: {@code overdueBalance * [(1 + TEDlate)^daysLate - 1]} (see {@code LateFeeCalculator}).</p>
 */
public record LateInterestRate(BigDecimal percentage, RateTypes type) {

    /** A nominal late rate capitalizes every day. */
    public static final int CAPITALIZATION_PERIOD_DAYS = 1;
    private static final int DAYS_PER_YEAR = 360;

    public LateInterestRate {
        percentage = InterestRate.requireValidPercentage(percentage, "LateInterestRate");
        if (Objects.isNull(type))
            throw new IllegalArgumentException("[LateInterestRate] type cannot be null");
    }

    /**
     * @return the effective daily late rate (TED of the moratorium rate) as a fraction:
     *         {@code (1 + TEA)^(1/360) - 1} for an effective rate, and
     *         {@code (1 + TNA * p / 360)^(1/p) - 1} with {@code p = 1} (that is, {@code TNA / 360}) for a nominal rate
     */
    public double effectiveDailyRate() {
        double annual = percentage.doubleValue() / 100;
        if (type == RateTypes.EFFECTIVE)
            return Math.pow(1 + annual, 1.0 / DAYS_PER_YEAR) - 1;
        return Math.pow(1 + annual * CAPITALIZATION_PERIOD_DAYS / DAYS_PER_YEAR, 1.0 / CAPITALIZATION_PERIOD_DAYS) - 1;
    }
}
