package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class InterestRateTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    void effectiveAnnualRateConvertsToDailyAndMonthlyWithBase360() {
        var tea12 = new InterestRate(new BigDecimal("12"), RateTypes.EFFECTIVE, null);

        assertThat(tea12.effectiveDailyRate()).isCloseTo(0.000314851459, within(TOLERANCE));
        assertThat(tea12.effectiveRateForDays(30)).isCloseTo(0.009488792935, within(TOLERANCE));
        assertThat(tea12.effectiveRateForDays(360)).isCloseTo(0.12, within(TOLERANCE));
    }

    @Test
    void nominalRateWithMonthlyCapitalizationGivesTnaOverTwelvePerMonth() {
        var tna24Monthly = new InterestRate(new BigDecimal("24"), RateTypes.NOMINAL, CapitalizationPeriods.MONTHLY);

        assertThat(tna24Monthly.effectiveRateForDays(30)).isCloseTo(0.02, within(TOLERANCE));
    }

    @Test
    void nominalRateWithDailyCapitalizationGivesTnaOver360PerDay() {
        var tna36Daily = new InterestRate(new BigDecimal("36"), RateTypes.NOMINAL, CapitalizationPeriods.DAILY);

        assertThat(tna36Daily.effectiveDailyRate()).isCloseTo(0.001, within(TOLERANCE));
    }

    @Test
    void zeroRateHasNoInterest() {
        var zero = new InterestRate(BigDecimal.ZERO, RateTypes.EFFECTIVE, null);

        assertThat(zero.effectiveRateForDays(30)).isZero();
    }

    @Test
    void percentageIsNormalizedToSevenDecimalsAndEffectiveDropsCapitalization() {
        var rate = new InterestRate(new BigDecimal("45.5"), RateTypes.EFFECTIVE, CapitalizationPeriods.MONTHLY);

        assertThat(rate.percentage()).isEqualByComparingTo("45.5000000");
        assertThat(rate.percentage().scale()).isEqualTo(7);
        assertThat(rate.capitalizationPeriod()).isNull();
    }

    @Test
    void nominalRateRequiresCapitalizationPeriod() {
        assertThatThrownBy(() -> new InterestRate(new BigDecimal("24"), RateTypes.NOMINAL, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeTooPreciseOrTooLargeRates() {
        assertThatThrownBy(() -> new InterestRate(new BigDecimal("-1"), RateTypes.EFFECTIVE, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InterestRate(new BigDecimal("12.12345678"), RateTypes.EFFECTIVE, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InterestRate(new BigDecimal("1000"), RateTypes.EFFECTIVE, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nominalLateRateCapitalizesDaily() {
        var late = new LateInterestRate(new BigDecimal("36"), RateTypes.NOMINAL);

        assertThat(late.effectiveDailyRate()).isCloseTo(0.001, within(TOLERANCE));
    }

    @Test
    void effectiveLateRateConvertsWithBase360() {
        var late = new LateInterestRate(new BigDecimal("12"), RateTypes.EFFECTIVE);

        assertThat(late.effectiveDailyRate()).isCloseTo(0.000314851459, within(TOLERANCE));
    }
}
