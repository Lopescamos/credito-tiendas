package pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.Instant;

/**
 * Table {@code operation_logs} (model: Log_Operacion, US-16).
 *
 * <p>No FK to {@code users}: the user bounded context is separate, and we want the log to
 * survive even if the user is later deleted (we don't delete, but the boundary must not couple
 * the schemas). An {@code action} enum is persisted by name and backed by a CHECK in V5.</p>
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class OperationLogPersistenceEntity extends AuditableAbstractPersistenceEntity {

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationActions action;

    @Column(nullable = false, length = 60)
    private String entity;

    private Long entityId;

    @Column(length = 500)
    private String detail;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private Instant occurredAt;
}
