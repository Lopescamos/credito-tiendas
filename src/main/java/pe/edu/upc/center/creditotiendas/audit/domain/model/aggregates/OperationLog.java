package pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.audit.domain.model.commands.RegisterOperationCommand;
import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;

/**
 * One entry in the system operations log (model: Log_Operacion, US-16).
 *
 * <p>Append-only: created by the AOP aspect right after a command service's handle(..) returns, and
 * never edited or deleted — the REST controller only exposes GET, and there is no update/delete
 * method on the repository port.</p>
 */
@Getter
public class OperationLog extends AbstractDomainAggregateRoot<OperationLog> {

    @Setter private Long id;
    /** Null when a sign-in attempt uses an email that does not match any user. */
    @Setter private Long userId;
    @Setter private OperationActions action;
    @Setter private String entity;
    /** Null when the operation does not target a single entity id (login, list operations, etc.). */
    @Setter private Long entityId;
    @Setter private String detail;
    @Setter private String ipAddress;
    @Setter private Instant occurredAt;

    public OperationLog() {
    }

    public OperationLog(RegisterOperationCommand command) {
        this.userId = command.userId();
        this.action = command.action();
        this.entity = command.entity();
        this.entityId = command.entityId();
        this.detail = command.detail();
        this.ipAddress = command.ipAddress();
        this.occurredAt = command.occurredAt() != null ? command.occurredAt() : Instant.now();
    }
}
