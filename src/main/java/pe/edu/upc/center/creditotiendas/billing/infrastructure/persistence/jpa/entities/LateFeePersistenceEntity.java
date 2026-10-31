package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Table {@code late_fees} (model: Cargo_Mora).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class LateFeePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "installment_id", nullable = false)
    private InstallmentPersistenceEntity installment;

    @Column(nullable = false)
    private int daysLate;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant calculatedAt;
}
