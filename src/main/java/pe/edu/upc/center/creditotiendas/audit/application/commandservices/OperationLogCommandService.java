package pe.edu.upc.center.creditotiendas.audit.application.commandservices;

import pe.edu.upc.center.creditotiendas.audit.domain.model.commands.RegisterOperationCommand;

/**
 * Entry point used by the audit aspect to persist operation log entries.
 *
 * <p>Exposes {@code void} because a failure to write an audit entry must never propagate: audit is
 * a side effect of the real command, not part of its contract. The implementation logs internally
 * and swallows any persistence error.</p>
 */
public interface OperationLogCommandService {
    void handle(RegisterOperationCommand command);
}
