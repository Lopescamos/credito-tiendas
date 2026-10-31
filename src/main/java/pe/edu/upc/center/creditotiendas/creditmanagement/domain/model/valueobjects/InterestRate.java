package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Annual compensatory interest rate agreed with a customer, expressed as a percentage with 7 decimals
 * ({@code 45.0000000} = 45 %). An EFFECTIVE rate is a TEA; a NOMINAL rate is a TNA that capitalizes every
 * {@link CapitalizationPeriods capitalization period}. Base year of 360 days.
 *
 * <p>Conversions to an effective daily rate (TED):</p>
 * <ul>
 *   <li>TEA: {@code TED = (1 + TEA)^(1/360) - 1}</li>
 *   <li>TNA capitalizing every {@code p} days: {@code TED = (1 + TNA * p / 360)^(1/p) - 1}</li>
 * </ul>
 * The effective rate of any {@code d}-day period is then {@code (1 + TED)^d - 1}.
 *
 * @param capitalizationPeriod required for NOMINAL rates, always {@code null} for EFFECTIVE rates
 */
public record InterestRate(BigDecimal percentage, RateTypes type, CapitalizationPeriods capitalizationPeriod) {

    public static final int RATE_SCALE = 7;
    static final BigDecimal MAX_PERCENTAGE = new BigDecimal("999.9999999");
    private static final int DAYS_PER_YEAR = 360;

    public InterestRate {
        percentage = requireValidPercentage(percentage, "InterestRate");
        if (Objects.isNull(type))
            throw new IllegalArgumentException("[InterestRate] type cannot be null");
        if (type == RateTypes.NOMINAL && Objects.isNull(capitalizationPeriod))
            throw new IllegalArgumentException("[InterestRate] a NOMINAL rate requires a capitalization period");
        if (type == RateTypes.EFFECTIVE)
            capitalizationPeriod = null;
    }

    /**
     * @return the effective daily rate (TED) as a fraction, e.g. {@code 0.000314851} for a 12 % TEA
     */
    public double effectiveDailyRate() {
        double annual = percentage.doubleValue() / 100;
        if (type == RateTypes.EFFECTIVE)
            return Math.pow(1 + annual, 1.0 / DAYS_PER_YEAR) - 1;
        int periodDays = capitalizationPeriod.getDays();
        return Math.pow(1 + annual * periodDays / DAYS_PER_YEAR, 1.0 / periodDays) - 1;
    }

    /**
     * @return the effective rate of a period of {@code days} days, as a fraction (30 days = TEM)
     */
    public double effectiveRateForDays(int days) {
        if (days < 0)
            throw new IllegalArgumentException("[InterestRate] days cannot be negative");
        return Math.pow(1 + effectiveDailyRate(), days) - 1;
    }

    static BigDecimal requireValidPercentage(BigDecimal percentage, String owner) {
        if (Objects.isNull(percentage))
            throw new IllegalArgumentException("[%s] percentage cannot be null".formatted(owner));
        if (percentage.signum() < 0)
            throw new IllegalArgumentException("[%s] percentage cannot be negative".formatted(owner));
        if (percentage.compareTo(MAX_PERCENTAGE) > 0)
            throw new IllegalArgumentException("[%s] percentage cannot exceed %s".formatted(owner, MAX_PERCENTAGE));
        if (percentage.stripTrailingZeros().scale() > RATE_SCALE)
            throw new IllegalArgumentException("[%s] percentage cannot have more than %d decimals".formatted(owner, RATE_SCALE));
        return percentage.setScale(RATE_SCALE, RoundingMode.UNNECESSARY);
    }
}
