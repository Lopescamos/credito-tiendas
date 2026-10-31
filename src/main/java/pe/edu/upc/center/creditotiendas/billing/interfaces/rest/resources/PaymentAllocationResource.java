package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources;

import java.math.BigDecimal;

public record PaymentAllocationResource(
        Long installmentId,
        BigDecimal lateFeeAmount,
        BigDecimal interestAmount,
        BigDecimal capitalAmount) {
}
