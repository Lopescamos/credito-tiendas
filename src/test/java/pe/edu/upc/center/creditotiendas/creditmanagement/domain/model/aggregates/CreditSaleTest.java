package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.SaleDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreditSaleTest {

    private static final LocalDate SALE_DATE = LocalDate.of(2026, 3, 1);

    @Test
    void registeringCopiesTheProfileRatesAndLaterEditsDoNotChangeTheSale() {
        var profile = profile(new BigDecimal("24"), new BigDecimal("1000"));
        var sale = CreditSale.register(profile, SALE_DATE, SaleModalities.INSTALLMENTS, 3, 0,
                List.of(new SaleDetail(1L, new BigDecimal("2"), new BigDecimal("500.00"))), null);
        var planAtSale = sale.paymentPlan();

        profile.updateConditions(
                new InterestRate(new BigDecimal("99"), RateTypes.EFFECTIVE, null),
                new LateInterestRate(new BigDecimal("80"), RateTypes.EFFECTIVE),
                1L, new BigDecimal("1000"), 360, 25, 5);

        assertThat(sale.getInterestRateSnapshot().percentage()).isEqualByComparingTo("24");
        assertThat(sale.getInterestRateSnapshot().type()).isEqualTo(RateTypes.NOMINAL);
        assertThat(sale.getInterestRateSnapshot().capitalizationPeriod()).isEqualTo(CapitalizationPeriods.MONTHLY);
        assertThat(sale.getLateInterestRateSnapshot().percentage()).isEqualByComparingTo("36");
        assertThat(sale.paymentPlan()).isEqualTo(planAtSale);
        assertThat(sale.getFixedInstallment()).isEqualByComparingTo("346.75");
    }

    @Test
    void totalIsTheSumOfRoundedSubtotalsAndTermIncludesGrace() {
        var sale = CreditSale.register(profile(new BigDecimal("24"), new BigDecimal("1000")), SALE_DATE,
                SaleModalities.INSTALLMENTS, 4, 10,
                List.of(new SaleDetail(1L, new BigDecimal("1.25"), new BigDecimal("3.33")),
                        new SaleDetail(2L, new BigDecimal("3"), new BigDecimal("10.00"))),
                null);

        assertThat(sale.getDetails().getFirst().getSubtotal()).isEqualByComparingTo("4.16");
        assertThat(sale.getTotalAmount()).isEqualByComparingTo("34.16");
        assertThat(sale.getTermDays()).isEqualTo(130);
        assertThat(sale.getStatus()).isEqualTo(CreditSaleStatuses.ACTIVE);
    }

    @Test
    void availableCreditOnlyCountsSalesNotFullyPaidAndIsNeverNegative() {
        var profile = profile(new BigDecimal("24"), new BigDecimal("1000"));
        var active = sale(profile, "300.00", CreditSaleStatuses.ACTIVE);
        var overdue = sale(profile, "200.00", CreditSaleStatuses.OVERDUE);
        var paid = sale(profile, "900.00", CreditSaleStatuses.PAID_ON_TIME);

        assertThat(profile.availableCredit(List.of(active, overdue, paid))).isEqualByComparingTo("500.00");

        profile.setCreditLimit(new BigDecimal("400.00"));
        assertThat(profile.availableCredit(List.of(active, overdue))).isEqualByComparingTo("0");
    }

    private static CreditSale sale(CreditProfile profile, String amount, CreditSaleStatuses status) {
        var sale = CreditSale.register(profile, SALE_DATE, SaleModalities.SINGLE_PAYMENT, 1, 0,
                List.of(new SaleDetail(1L, BigDecimal.ONE, new BigDecimal(amount))), null);
        sale.setStatus(status);
        return sale;
    }

    private static CreditProfile profile(BigDecimal ratePercentage, BigDecimal creditLimit) {
        var profile = new CreditProfile();
        profile.setId(1L);
        profile.setInterestRate(new InterestRate(ratePercentage, RateTypes.NOMINAL, CapitalizationPeriods.MONTHLY));
        profile.setLateInterestRate(new LateInterestRate(new BigDecimal("36"), RateTypes.NOMINAL));
        profile.setCreditLimit(creditLimit);
        return profile;
    }
}
