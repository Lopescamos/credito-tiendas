package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * @param generatedSaleId the sale created when the request was approved (US-09 traceability)
 */
public record CreditPurchaseRequestResource(
        Long id,
        Long creditProfileId,
        Instant requestedAt,
        String status,
        String rejectionReason,
        Instant respondedAt,
        Long generatedSaleId,
        BigDecimal totalAmount,
        List<LineItemResource> details) {
}
