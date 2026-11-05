package pe.edu.upc.center.creditotiendas.audit.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.audit.application.queryservices.OperationLogQueryService;
import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.domain.model.queries.GetOperationLogsQuery;
import pe.edu.upc.center.creditotiendas.audit.domain.repositories.OperationLogRepository;

import java.util.List;

@Service
public class OperationLogQueryServiceImpl implements OperationLogQueryService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogQueryServiceImpl(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Override
    public List<OperationLog> handle(GetOperationLogsQuery query) {
        return operationLogRepository.search(query);
    }
}
