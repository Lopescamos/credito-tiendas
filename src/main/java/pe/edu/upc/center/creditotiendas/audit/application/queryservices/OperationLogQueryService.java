package pe.edu.upc.center.creditotiendas.audit.application.queryservices;

import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.domain.model.queries.GetOperationLogsQuery;

import java.util.List;

public interface OperationLogQueryService {
    List<OperationLog> handle(GetOperationLogsQuery query);
}
