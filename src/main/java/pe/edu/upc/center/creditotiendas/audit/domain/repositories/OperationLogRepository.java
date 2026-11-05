package pe.edu.upc.center.creditotiendas.audit.domain.repositories;

import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.domain.model.queries.GetOperationLogsQuery;

import java.util.List;

/**
 * Persistence port of the {@link OperationLog} aggregate. Append-only on purpose: there is no
 * update or delete method here, because US-16 forbids editing or deleting log entries.
 */
public interface OperationLogRepository {
    OperationLog save(OperationLog operationLog);
    List<OperationLog> search(GetOperationLogsQuery query);
}
