package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A credit sale with the snapshot of the 5 rate columns taken when it was registered.
 */
public record CreditSaleResource(
        Long id,
        Long creditProfileId,
        LocalDate saleDate,
        BigDecimal totalAmount,
        String modality,
        int termDays,
        int graceDays,
        int installmentCount,
        BigDecimal fixedInstallment,
        BigDecimal financedCapital,
        BigDecimal interestRateSnapshot,
        String rateTypeSnapshot,
        String capitalizationPeriodSnapshot,
        BigDecimal lateInterestRateSnapshot,
        String lateInterestRateTypeSnapshot,
        String status,
        Long originPurchaseRequestId,
        List<LineItemResource> details) {
}
