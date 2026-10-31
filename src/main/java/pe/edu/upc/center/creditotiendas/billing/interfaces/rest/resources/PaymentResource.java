package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PaymentResource(
        Long id,
        Long creditProfileId,
        BigDecimal totalAmount,
        LocalDate paymentDate,
        String paymentMethod,
        String reference,
        Instant registeredAt,
        List<PaymentAllocationResource> allocations) {
}
