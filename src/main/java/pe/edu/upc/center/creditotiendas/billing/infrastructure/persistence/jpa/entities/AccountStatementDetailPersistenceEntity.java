package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.MovementTypes;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Table {@code account_statement_details} (model: Detalle_Estado_Cuenta).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class AccountStatementDetailPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_statement_id", nullable = false)
    private AccountStatementPersistenceEntity accountStatement;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private MovementTypes movementType;

    @Column(length = 200, nullable = false)
    private String description;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;
}
