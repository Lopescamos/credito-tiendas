package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RateTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Expected values were computed independently with Python's {@code decimal} module (40 digits of precision).
 */
class LateFeeCalculatorTest {

    private static final LocalDate DUE_DATE = LocalDate.of(2026, 2, 14);
    private static final BigDecimal INSTALLMENT = new BigDecimal("346.75");
    /** Nominal 36 % capitalizing daily: TED = 0.36 / 360 = 0.001. */
    private static final LateInterestRate TNA_36 = new LateInterestRate(new BigDecimal("36"), RateTypes.NOMINAL);
    private static final LateInterestRate TEA_60 = new LateInterestRate(new BigDecimal("60"), RateTypes.EFFECTIVE);

    @Test
    void paymentExactlyOnTheDueDateHasNoLateFee() {
        assertThat(LateFeeCalculator.daysLate(DUE_DATE, DUE_DATE)).isZero();
        assertThat(LateFeeCalculator.calculate(INSTALLMENT, TNA_36, DUE_DATE, DUE_DATE)).isEqualByComparingTo("0.00");
    }

    @Test
    void paymentBeforeTheDueDateHasNoLateFee() {
        assertThat(LateFeeCalculator.calculate(INSTALLMENT, TNA_36, DUE_DATE, DUE_DATE.minusDays(5)))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void oneDayLateAlreadyAccruesLateFee() {
        assertThat(LateFeeCalculator.calculate(INSTALLMENT, TNA_36, DUE_DATE, DUE_DATE.plusDays(1)))
                .isEqualByComparingTo("0.35");
    }

    @ParameterizedTest
    @CsvSource({"10, 3.48", "20, 7.00", "30, 10.55"})
    void nominalLateRateCompoundsDaily(int daysLate, String expectedFee) {
        assertThat(LateFeeCalculator.calculate(INSTALLMENT, TNA_36, DUE_DATE, DUE_DATE.plusDays(daysLate)))
                .isEqualByComparingTo(expectedFee);
    }

    @ParameterizedTest
    @CsvSource({"5, 3.34", "45, 30.86"})
    void effectiveLateRateConvertsWithBase360(int daysLate, String expectedFee) {
        assertThat(LateFeeCalculator.calculate(new BigDecimal("510.00"), TEA_60, DUE_DATE, DUE_DATE.plusDays(daysLate)))
                .isEqualByComparingTo(expectedFee);
    }

    /**
     * Late fee on top of late fee: charging 10 days and then compounding the next 10 days on (installment + first
     * fee) gives the same as charging 20 days at once. Recomputing on a later day must replace the earlier fee,
     * never add a second full fee.
     */
    @Test
    void lateFeeOnLateFeeMatchesComputingAllDaysAtOnce() {
        var firstTenDays = LateFeeCalculator.calculate(INSTALLMENT, TNA_36, DUE_DATE, DUE_DATE.plusDays(10));
        var nextTenDaysOnTop = LateFeeCalculator.calculate(INSTALLMENT.add(firstTenDays), TNA_36, DUE_DATE, DUE_DATE.plusDays(10));
        var twentyDays = LateFeeCalculator.calculate(INSTALLMENT, TNA_36, DUE_DATE, DUE_DATE.plusDays(20));

        assertThat(firstTenDays.add(nextTenDaysOnTop)).isEqualByComparingTo(twentyDays);
        assertThat(twentyDays).isLessThan(firstTenDays.add(firstTenDays).add(firstTenDays));
    }

    @Test
    void eachOverdueInstallmentUsesOnlyItsOwnBalanceAndDays() {
        var olderDue = DUE_DATE;
        var newerDue = DUE_DATE.plusDays(30);
        var paymentDate = DUE_DATE.plusDays(40);

        var olderFee = LateFeeCalculator.calculate(INSTALLMENT, TNA_36, olderDue, paymentDate);
        var newerFee = LateFeeCalculator.calculate(INSTALLMENT, TNA_36, newerDue, paymentDate);

        assertThat(LateFeeCalculator.daysLate(olderDue, paymentDate)).isEqualTo(40);
        assertThat(LateFeeCalculator.daysLate(newerDue, paymentDate)).isEqualTo(10);
        assertThat(newerFee).isEqualByComparingTo("3.48");
        assertThat(olderFee).isGreaterThan(newerFee);
    }

    @Test
    void zeroLateRateOrZeroBalanceHasNoLateFee() {
        var zeroRate = new LateInterestRate(BigDecimal.ZERO, RateTypes.NOMINAL);

        assertThat(LateFeeCalculator.calculate(INSTALLMENT, zeroRate, DUE_DATE, DUE_DATE.plusDays(30))).isEqualByComparingTo("0.00");
        assertThat(LateFeeCalculator.calculate(BigDecimal.ZERO, TNA_36, DUE_DATE, DUE_DATE.plusDays(30))).isEqualByComparingTo("0.00");
    }

    @Test
    void nominalLateRateCapitalizationIsAFixedDailyRule() {
        assertThat(LateInterestRate.CAPITALIZATION_PERIOD_DAYS).isEqualTo(1);
    }

    @Test
    void rejectsNegativeBalance() {
        assertThatThrownBy(() -> LateFeeCalculator.calculate(new BigDecimal("-1"), TNA_36, DUE_DATE, DUE_DATE.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
