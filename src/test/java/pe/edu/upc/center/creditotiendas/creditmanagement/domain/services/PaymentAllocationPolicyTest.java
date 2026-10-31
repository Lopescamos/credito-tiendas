package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import org.junit.jupiter.api.Test;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InstallmentDue;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentAllocation;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentComponents.*;

class PaymentAllocationPolicyTest {

    /** Installment #1 of the French plan of 1000.00 at 2 % monthly, 10 days late at 36 % nominal. */
    private static final InstallmentDue FIRST_LATE = due(1, "3.48", "20.00", "326.75");
    private static final InstallmentDue SECOND_ON_TIME = due(2, "0.00", "13.47", "333.28");

    @Test
    void appliesLateFeeThenInterestThenCapital() {
        var allocations = PaymentAllocationPolicy.allocate(List.of(FIRST_LATE), new BigDecimal("350.23"));

        assertThat(allocations).containsExactly(
                new PaymentAllocation(1, LATE_FEE, new BigDecimal("3.48")),
                new PaymentAllocation(1, INTEREST, new BigDecimal("20.00")),
                new PaymentAllocation(1, CAPITAL, new BigDecimal("326.75")));
    }

    @Test
    void withSeveralInstallmentsAllLateFeesGoFirstThenInterestsThenCapitalOldestFirst() {
        var allocations = PaymentAllocationPolicy.allocate(List.of(SECOND_ON_TIME, FIRST_LATE), new BigDecimal("696.98"));

        assertThat(allocations).extracting(PaymentAllocation::component, PaymentAllocation::installmentNumber)
                .containsExactly(
                        tuple(LATE_FEE, 1),
                        tuple(INTEREST, 1),
                        tuple(INTEREST, 2),
                        tuple(CAPITAL, 1),
                        tuple(CAPITAL, 2));
        assertThat(allocations.stream().map(PaymentAllocation::amount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("696.98");
    }

    @Test
    void installmentPaidOnTimeHasNoLateFeeAllocation() {
        var allocations = PaymentAllocationPolicy.allocate(List.of(SECOND_ON_TIME), new BigDecimal("346.75"));

        assertThat(allocations).extracting(PaymentAllocation::component).containsExactly(INTEREST, CAPITAL);
    }

    @Test
    void rejectsPartialPaymentEvenByOneCent() {
        assertThatThrownBy(() -> PaymentAllocationPolicy.allocate(List.of(FIRST_LATE), new BigDecimal("350.22")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partial payments are not allowed");
    }

    @Test
    void rejectsOverpaymentEvenByOneCent() {
        assertThatThrownBy(() -> PaymentAllocationPolicy.allocate(List.of(FIRST_LATE), new BigDecimal("350.24")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsPayingOnlyTheCapitalAndSkippingTheLateFee() {
        assertThatThrownBy(() -> PaymentAllocationPolicy.allocate(List.of(FIRST_LATE), new BigDecimal("326.75")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptySelectionOrNonPositivePayment() {
        assertThatThrownBy(() -> PaymentAllocationPolicy.allocate(List.of(), BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaymentAllocationPolicy.allocate(List.of(FIRST_LATE), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void totalOwedAddsEveryComponentOfEveryInstallment() {
        assertThat(PaymentAllocationPolicy.totalOwed(List.of(FIRST_LATE, SECOND_ON_TIME))).isEqualByComparingTo("696.98");
    }

    private static InstallmentDue due(int number, String lateFee, String interest, String capital) {
        return new InstallmentDue(number, new BigDecimal(lateFee), new BigDecimal(interest), new BigDecimal(capital));
    }
}
