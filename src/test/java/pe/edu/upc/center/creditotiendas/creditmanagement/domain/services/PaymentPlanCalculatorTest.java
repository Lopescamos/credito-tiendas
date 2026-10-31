package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CapitalizationPeriods;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentPlan;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RateTypes;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.ScheduledInstallment;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Expected values were computed independently with Python's {@code decimal} module (40 digits of precision).
 */
class PaymentPlanCalculatorTest {

    private static final LocalDate SALE_DATE = LocalDate.of(2026, 1, 15);
    private static final InterestRate TNA_24_MONTHLY =
            new InterestRate(new BigDecimal("24"), RateTypes.NOMINAL, CapitalizationPeriods.MONTHLY);
    private static final InterestRate TEA_12 = new InterestRate(new BigDecimal("12"), RateTypes.EFFECTIVE, null);

    @Test
    void frenchMethodWithoutGraceMatchesHandCalculation() {
        var plan = PaymentPlanCalculator.calculate(new BigDecimal("1000"), TNA_24_MONTHLY, 0, 3, SALE_DATE);

        assertThat(plan.financedCapital()).isEqualByComparingTo("1000.00");
        assertThat(plan.fixedInstallment()).isEqualByComparingTo("346.75");
        assertInstallment(plan, 1, "20.00", "326.75", "346.75", "673.25");
        assertInstallment(plan, 2, "13.47", "333.28", "346.75", "339.97");
        assertInstallment(plan, 3, "6.80", "339.97", "346.77", "0.00");
    }

    @Test
    void zeroGraceDaysKeepTheSaleAmountAsFinancedCapital() {
        var plan = PaymentPlanCalculator.calculate(new BigDecimal("1234.56"), TEA_12, 0, 4, SALE_DATE);

        assertThat(plan.financedCapital()).isEqualByComparingTo("1234.56");
    }

    @Test
    void totalGraceCapitalizesInterestOfTheGraceDays() {
        var thirtyDays = PaymentPlanCalculator.calculate(new BigDecimal("1000"), TNA_24_MONTHLY, 30, 3, SALE_DATE);
        var fifteenDays = PaymentPlanCalculator.calculate(new BigDecimal("1000"), TNA_24_MONTHLY, 15, 3, SALE_DATE);

        assertThat(thirtyDays.financedCapital()).isEqualByComparingTo("1020.00");
        assertThat(fifteenDays.financedCapital()).isEqualByComparingTo("1009.95");
    }

    @Test
    void installmentsAreDueEvery30DaysAfterTheGraceDays() {
        var plan = PaymentPlanCalculator.calculate(new BigDecimal("1000"), TNA_24_MONTHLY, 10, 3, SALE_DATE);

        assertThat(plan.installments()).extracting(ScheduledInstallment::dueDate).containsExactly(
                SALE_DATE.plusDays(40), SALE_DATE.plusDays(70), SALE_DATE.plusDays(100));
    }

    @Test
    void effectiveRateWithGraceMatchesIndependentCalculation() {
        var plan = PaymentPlanCalculator.calculate(new BigDecimal("1200"), TEA_12, 10, 6, SALE_DATE);

        assertThat(plan.financedCapital()).isEqualByComparingTo("1203.78");
        assertThat(plan.fixedInstallment()).isEqualByComparingTo("207.35");
        assertInstallment(plan, 1, "11.42", "195.93", "207.35", "1007.85");
        assertInstallment(plan, 2, "9.56", "197.79", "207.35", "810.06");
        assertInstallment(plan, 3, "7.69", "199.66", "207.35", "610.40");
        assertInstallment(plan, 4, "5.79", "201.56", "207.35", "408.84");
        assertInstallment(plan, 5, "3.88", "203.47", "207.35", "205.37");
        assertInstallment(plan, 6, "1.95", "205.37", "207.32", "0.00");
    }

    @Test
    void zeroInterestSplitsCapitalAndLastInstallmentAbsorbsRounding() {
        var zeroRate = new InterestRate(BigDecimal.ZERO, RateTypes.EFFECTIVE, null);

        var plan = PaymentPlanCalculator.calculate(new BigDecimal("1000"), zeroRate, 0, 3, SALE_DATE);

        assertThat(plan.fixedInstallment()).isEqualByComparingTo("333.33");
        assertInstallment(plan, 1, "0.00", "333.33", "333.33", "666.67");
        assertInstallment(plan, 2, "0.00", "333.33", "333.33", "333.34");
        assertInstallment(plan, 3, "0.00", "333.34", "333.34", "0.00");
    }

    @Test
    void singlePaymentIsOneInstallmentAfterOneMonth() {
        var plan = PaymentPlanCalculator.calculate(new BigDecimal("500"), TNA_24_MONTHLY, 0, 1, SALE_DATE);

        assertThat(plan.installments()).hasSize(1);
        assertInstallment(plan, 1, "10.00", "500.00", "510.00", "0.00");
        assertThat(plan.installments().getFirst().dueDate()).isEqualTo(SALE_DATE.plusDays(30));
    }

    @ParameterizedTest
    @CsvSource({
            "1000.00, 0, 3",
            "1500.75, 7, 6",
            "99.99, 45, 12",
            "25000.00, 0, 24",
            "0.01, 0, 1",
            "3333.33, 29, 5"
    })
    void capitalAlwaysAddsUpToFinancedCapitalAndEndsAtZero(BigDecimal principal, int graceDays, int installments) {
        for (var rate : new InterestRate[]{TNA_24_MONTHLY, TEA_12,
                new InterestRate(new BigDecimal("89.9999999"), RateTypes.NOMINAL, CapitalizationPeriods.DAILY)}) {
            var plan = PaymentPlanCalculator.calculate(principal, rate, graceDays, installments, SALE_DATE);

            var totalCapital = plan.installments().stream()
                    .map(ScheduledInstallment::capital).reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(totalCapital).isEqualByComparingTo(plan.financedCapital());
            assertThat(plan.installments().getLast().remainingBalance()).isEqualByComparingTo("0");
            assertThat(plan.installments()).allSatisfy(installment -> {
                assertThat(installment.capital().signum()).isGreaterThanOrEqualTo(0);
                assertThat(installment.amount()).isEqualByComparingTo(installment.interest().add(installment.capital()));
            });
            assertThat(plan.totalToPay()).isEqualByComparingTo(plan.financedCapital().add(plan.totalInterest()));
        }
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> PaymentPlanCalculator.calculate(BigDecimal.ZERO, TEA_12, 0, 1, SALE_DATE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaymentPlanCalculator.calculate(BigDecimal.TEN, TEA_12, -1, 1, SALE_DATE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaymentPlanCalculator.calculate(BigDecimal.TEN, TEA_12, 0, 0, SALE_DATE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static void assertInstallment(PaymentPlan plan, int number, String interest, String capital,
                                          String amount, String remainingBalance) {
        var installment = plan.installments().get(number - 1);
        assertThat(installment.number()).isEqualTo(number);
        assertThat(installment.interest()).as("interest of #%d", number).isEqualByComparingTo(interest);
        assertThat(installment.capital()).as("capital of #%d", number).isEqualByComparingTo(capital);
        assertThat(installment.amount()).as("amount of #%d", number).isEqualByComparingTo(amount);
        assertThat(installment.remainingBalance()).as("balance after #%d", number).isEqualByComparingTo(remainingBalance);
    }
}
