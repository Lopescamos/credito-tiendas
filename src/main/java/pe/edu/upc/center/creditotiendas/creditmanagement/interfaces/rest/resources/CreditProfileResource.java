package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * @param availableCredit credit limit minus the sales not fully paid yet
 */
public record CreditProfileResource(
        Long id,
        Long userId,
        Long storeId,
        BigDecimal interestRate,
        String rateType,
        String capitalizationPeriod,
        BigDecimal lateInterestRate,
        String lateInterestRateType,
        Long currencyId,
        BigDecimal creditLimit,
        BigDecimal availableCredit,
        int maxTermDays,
        int cutoffDay,
        int paymentDay,
        boolean active) {
}
