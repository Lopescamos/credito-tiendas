package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CreditSaleStatuses.*;
import static pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityFailures.*;

class PurchaseRequestEligibilityPolicyTest {

    private static final BigDecimal AVAILABLE = new BigDecimal("500.00");

    @Test
    void eligibleWhenActiveWithAPaidOnTimeSaleNoOverdueAndAmountWithinCredit() {
        var assessment = PurchaseRequestEligibilityPolicy.assess(
                true, List.of(PAID_ON_TIME, ACTIVE), AVAILABLE, new BigDecimal("500.00"));

        assertThat(assessment.eligible()).isTrue();
    }

    @Test
    void notEligibleWithoutAnyPreviousSalePaidOnTime() {
        var neverBought = PurchaseRequestEligibilityPolicy.assess(true, List.of(), AVAILABLE, null);
        var onlyPaidLate = PurchaseRequestEligibilityPolicy.assess(true, List.of(PAID_LATE, ACTIVE), AVAILABLE, null);

        assertThat(neverBought.failures()).containsExactly(NO_SALE_PAID_ON_TIME);
        assertThat(onlyPaidLate.failures()).containsExactly(NO_SALE_PAID_ON_TIME);
    }

    @Test
    void notEligibleWithOverdueInstallments() {
        var assessment = PurchaseRequestEligibilityPolicy.assess(
                true, List.of(PAID_ON_TIME, OVERDUE), AVAILABLE, null);

        assertThat(assessment.failures()).containsExactly(HAS_OVERDUE_INSTALLMENTS);
    }

    @Test
    void notEligibleWhenAmountExceedsAvailableCreditByOneCent() {
        var assessment = PurchaseRequestEligibilityPolicy.assess(
                true, List.of(PAID_ON_TIME), AVAILABLE, new BigDecimal("500.01"));

        assertThat(assessment.failures()).containsExactly(AMOUNT_EXCEEDS_AVAILABLE_CREDIT);
    }

    @Test
    void reportsEveryFailedRule() {
        var assessment = PurchaseRequestEligibilityPolicy.assess(
                false, List.of(OVERDUE), AVAILABLE, new BigDecimal("900"));

        assertThat(assessment.eligible()).isFalse();
        assertThat(assessment.failures()).containsExactly(
                PROFILE_INACTIVE, NO_SALE_PAID_ON_TIME, HAS_OVERDUE_INSTALLMENTS, AMOUNT_EXCEEDS_AVAILABLE_CREDIT);
    }

    @Test
    void withoutAmountOnlyChecksWhetherTheCustomerMayRequest() {
        var assessment = PurchaseRequestEligibilityPolicy.assess(
                true, List.of(PAID_ON_TIME), BigDecimal.ZERO, null);

        assertThat(assessment.eligible()).isTrue();
    }
}
