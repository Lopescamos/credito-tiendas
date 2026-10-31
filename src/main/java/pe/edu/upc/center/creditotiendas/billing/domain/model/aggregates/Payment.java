package pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.billing.domain.model.entities.PaymentAllocation;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.PaymentMethods;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A customer payment (model: Pago_Realizado + Imputacion_Pago, US-05, US-10). Covers a set of installments
 * exactly, with mandatory prelation {@code mora → interés → capital}. {@code reference} is the external
 * reference of the payment method (voucher, Yape operation code, ...).
 */
@Getter
public class Payment extends AbstractDomainAggregateRoot<Payment> {

    @Setter private Long id;
    @Setter private Long creditProfileId;
    @Setter private BigDecimal totalAmount;
    @Setter private LocalDate paymentDate;
    @Setter private PaymentMethods paymentMethod;
    @Setter private String reference;
    @Setter private Instant registeredAt;
    private final List<PaymentAllocation> allocations = new ArrayList<>();

    public Payment() {
    }

    public Payment(Long creditProfileId, BigDecimal totalAmount, LocalDate paymentDate, PaymentMethods paymentMethod,
                   String reference, List<PaymentAllocation> allocations) {
        this.creditProfileId = creditProfileId;
        this.totalAmount = totalAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.reference = reference;
        this.registeredAt = Instant.now();
        this.allocations.addAll(allocations);
    }

    public List<PaymentAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }

    /**
     * Replaces the allocations. Used by the persistence assembler to rebuild the aggregate.
     */
    public void setAllocations(List<PaymentAllocation> allocations) {
        this.allocations.clear();
        this.allocations.addAll(allocations);
    }
}
