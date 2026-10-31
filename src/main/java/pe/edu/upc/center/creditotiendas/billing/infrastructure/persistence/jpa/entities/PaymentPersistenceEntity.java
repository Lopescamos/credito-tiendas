package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.PaymentMethods;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Table {@code payments} (model: Pago_Realizado), with its allocations.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class PaymentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long creditProfileId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentMethods paymentMethod;

    @Column(length = 100)
    private String reference;

    @Column(nullable = false)
    private Instant registeredAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PaymentAllocationPersistenceEntity> allocations = new ArrayList<>();
}
