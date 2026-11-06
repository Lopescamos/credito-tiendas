package pe.edu.upc.center.creditotiendas.audit.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.interfaces.rest.resources.OperationLogResource;

public final class OperationLogResourceFromEntityAssembler {

    private OperationLogResourceFromEntityAssembler() {
    }

    public static OperationLogResource toResourceFromEntity(OperationLog log) {
        return new OperationLogResource(
                log.getId(),
                log.getUserId(),
                log.getAction().name(),
                log.getEntity(),
                log.getEntityId(),
                log.getDetail(),
                log.getIpAddress(),
                log.getOccurredAt());
    }
}
