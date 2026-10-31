package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentPlan;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.ScheduledInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Builds the payment plan of a credit sale with the ordinary French method (installments in arrears),
 * 360-day year and 30-day months.
 *
 * <ol>
 *   <li>Total grace: the grace days capitalize at the daily effective rate:
 *       {@code C' = C0 * (1 + TED)^dg}.</li>
 *   <li>Fixed installment with the 30-day effective rate {@code i}:
 *       {@code R = C' * i / (1 - (1 + i)^-n)}; with {@code i = 0}, {@code R = C' / n}.</li>
 *   <li>Each installment: {@code interest = balance * i}, {@code capital = R - interest}. The last
 *       installment pays the whole remaining balance, so rounding never leaves a residue.</li>
 *   <li>Installment {@code k} is due {@code dg + 30k} days after the sale date.</li>
 * </ol>
 *
 * <p>Amounts are rounded to 2 decimals (HALF_UP) at each step, as they are stored as DECIMAL(12,2).
 * Rates and factors keep 12 decimals.</p>
 */
public final class PaymentPlanCalculator {

    public static final int MONEY_SCALE = 2;
    public static final int PERIOD_DAYS = 30;
    private static final int FACTOR_SCALE = 12;

    private PaymentPlanCalculator() {
    }

    public static PaymentPlan calculate(BigDecimal principal, InterestRate rate, int graceDays,
                                        int installmentCount, LocalDate saleDate) {
        if (Objects.isNull(principal) || principal.signum() <= 0)
            throw new IllegalArgumentException("[PaymentPlanCalculator] principal must be greater than zero");
        if (Objects.isNull(rate))
            throw new IllegalArgumentException("[PaymentPlanCalculator] rate cannot be null");
        if (graceDays < 0)
            throw new IllegalArgumentException("[PaymentPlanCalculator] grace days cannot be negative");
        if (installmentCount < 1)
            throw new IllegalArgumentException("[PaymentPlanCalculator] there must be at least one installment");
        if (Objects.isNull(saleDate))
            throw new IllegalArgumentException("[PaymentPlanCalculator] sale date cannot be null");

        double dailyRate = rate.effectiveDailyRate();
        var financedCapital = money(principal.multiply(factor(Math.pow(1 + dailyRate, graceDays))));

        double periodRate = rate.effectiveRateForDays(PERIOD_DAYS);
        var periodRateDecimal = factor(periodRate);
        var fixedInstallment = periodRateDecimal.signum() == 0
                ? money(financedCapital.divide(BigDecimal.valueOf(installmentCount), FACTOR_SCALE, RoundingMode.HALF_UP))
                : money(financedCapital.multiply(factor(
                        periodRate / (1 - Math.pow(1 + periodRate, -installmentCount)))));

        var balance = financedCapital;
        var installments = new ArrayList<ScheduledInstallment>(installmentCount);
        for (int number = 1; number <= installmentCount; number++) {
            var interest = money(balance.multiply(periodRateDecimal));
            var capital = number == installmentCount ? balance : fixedInstallment.subtract(interest);
            balance = balance.subtract(capital);
            installments.add(new ScheduledInstallment(
                    number,
                    saleDate.plusDays(graceDays + (long) PERIOD_DAYS * number),
                    interest,
                    capital,
                    interest.add(capital),
                    balance));
        }
        return new PaymentPlan(financedCapital, fixedInstallment, installments);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Rates and growth factors come from {@code Math.pow} on doubles, which leaves noise around the
     * 16th digit (a 2 % monthly rate comes out as {@code 0.020000000000000684}). Rounding them to 12
     * decimals removes that noise, so it can never flip a cent when an amount ends exactly in 5.
     */
    private static BigDecimal factor(double value) {
        return BigDecimal.valueOf(value).setScale(FACTOR_SCALE, RoundingMode.HALF_UP);
    }
}
