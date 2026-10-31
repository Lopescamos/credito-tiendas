package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PurchaseRequestStatuses;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Table {@code credit_purchase_requests} (model: Solicitud_Compra_Credito).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class CreditPurchaseRequestPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long creditProfileId;

    @Column(nullable = false)
    private Instant requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PurchaseRequestStatuses status;

    @Column(length = 300)
    private String rejectionReason;

    private Instant respondedAt;

    private Long generatedSaleId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseRequestDetailPersistenceEntity> details = new ArrayList<>();
}
