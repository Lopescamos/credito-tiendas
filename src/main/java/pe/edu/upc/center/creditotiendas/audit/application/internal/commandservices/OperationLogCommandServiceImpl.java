package pe.edu.upc.center.creditotiendas.audit.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.center.creditotiendas.audit.application.commandservices.OperationLogCommandService;
import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.domain.model.commands.RegisterOperationCommand;
import pe.edu.upc.center.creditotiendas.audit.domain.repositories.OperationLogRepository;

/**
 * Writes an entry in the operation log on behalf of the AOP aspect.
 *
 * <p>Runs in a {@code REQUIRES_NEW} transaction so the audit entry survives even if the caller's
 * own transaction later rolls back (the business operation already succeeded at the point the
 * aspect fires — the audit trail should still record it). Any persistence error is swallowed and
 * logged, never propagated, because an audit failure must not break the business command.</p>
 */
@Slf4j
@Service
public class OperationLogCommandServiceImpl implements OperationLogCommandService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogCommandServiceImpl(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(RegisterOperationCommand command) {
        try {
            operationLogRepository.save(new OperationLog(command));
        } catch (Exception e) {
            log.warn("Operation log entry could not be persisted (action={}, entity={}): {}",
                    command.action(), command.entity(), e.getMessage());
        }
    }
}
