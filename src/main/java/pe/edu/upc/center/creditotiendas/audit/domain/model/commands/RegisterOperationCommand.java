package pe.edu.upc.center.creditotiendas.audit.domain.model.commands;

import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;

import java.time.Instant;
import java.util.Objects;

/**
 * Registers one entry in the operation log (US-16). The aspect fills this and sends it to Audit
 * after every audited command handler returns.
 *
 * @param userId     who executed the operation, or {@code null} when it failed to identify the user
 *                   (e.g. sign-in attempt with an unknown email)
 * @param action     what verb was performed, derived from the command's class name
 * @param entity     the aggregate name affected, also derived from the command class
 * @param entityId   affected aggregate id, or {@code null} when unknown
 * @param detail     short description — the command's simple class name by default
 * @param ipAddress  client IP read from the current HTTP request, {@code null} outside of one
 * @param occurredAt when the operation happened; {@code null} means "now"
 */
public record RegisterOperationCommand(Long userId, OperationActions action, String entity, Long entityId,
                                       String detail, String ipAddress, Instant occurredAt) {
    public RegisterOperationCommand {
        Objects.requireNonNull(action, "[RegisterOperationCommand] action cannot be null");
        if (entity == null || entity.isBlank())
            throw new IllegalArgumentException("[RegisterOperationCommand] entity cannot be blank");
    }
}
