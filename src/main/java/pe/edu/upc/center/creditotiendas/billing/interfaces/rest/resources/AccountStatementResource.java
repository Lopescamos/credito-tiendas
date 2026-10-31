package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountStatementResource(
        Long id,
        Long creditProfileId,
        String period,
        LocalDate cutoffDate,
        BigDecimal previousBalance,
        BigDecimal periodPurchases,
        BigDecimal periodPayments,
        BigDecimal periodInterest,
        BigDecimal periodLateFees,
        BigDecimal finalBalance,
        List<AccountStatementDetailResource> details) {
}
