package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;

/**
 * Table {@code payment_allocations} (model: Imputacion_Pago).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class PaymentAllocationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentPersistenceEntity payment;

    @Column(nullable = false)
    private Long installmentId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal lateFeeAmount;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal interestAmount;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal capitalAmount;
}
