package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;

/**
 * Table {@code purchase_request_details} (model: Detalle_Solicitud).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class PurchaseRequestDetailPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_request_id", nullable = false)
    private CreditPurchaseRequestPersistenceEntity purchaseRequest;

    @Column(nullable = false)
    private Long productId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;
}
