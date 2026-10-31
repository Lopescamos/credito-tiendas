package pe.edu.upc.center.creditotiendas.billing.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Row of {@code Imputacion_Pago}: how much of a payment covers the late fee, interest and capital of one
 * installment. Entity under the {@link pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment}
 * aggregate.
 */
@Getter
public class PaymentAllocation {

    @Setter private Long id;
    @Setter private Long installmentId;
    @Setter private BigDecimal lateFeeAmount;
    @Setter private BigDecimal interestAmount;
    @Setter private BigDecimal capitalAmount;

    public PaymentAllocation() {
        this.lateFeeAmount = BigDecimal.ZERO;
        this.interestAmount = BigDecimal.ZERO;
        this.capitalAmount = BigDecimal.ZERO;
    }

    public PaymentAllocation(Long installmentId, BigDecimal lateFeeAmount, BigDecimal interestAmount, BigDecimal capitalAmount) {
        this.installmentId = installmentId;
        this.lateFeeAmount = lateFeeAmount;
        this.interestAmount = interestAmount;
        this.capitalAmount = capitalAmount;
    }

    public BigDecimal total() {
        return lateFeeAmount.add(interestAmount).add(capitalAmount);
    }
}
