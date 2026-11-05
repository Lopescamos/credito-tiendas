package pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.entities.OperationLogPersistenceEntity;

public final class OperationLogPersistenceAssembler {

    private OperationLogPersistenceAssembler() {
    }

    public static OperationLog toDomainFromPersistence(OperationLogPersistenceEntity entity) {
        if (entity == null) return null;
        var log = new OperationLog();
        log.setId(entity.getId());
        log.setUserId(entity.getUserId());
        log.setAction(entity.getAction());
        log.setEntity(entity.getEntity());
        log.setEntityId(entity.getEntityId());
        log.setDetail(entity.getDetail());
        log.setIpAddress(entity.getIpAddress());
        log.setOccurredAt(entity.getOccurredAt());
        return log;
    }

    public static OperationLogPersistenceEntity toPersistenceFromDomain(OperationLog log) {
        if (log == null) return null;
        var entity = new OperationLogPersistenceEntity();
        if (log.getId() != null) entity.setId(log.getId());
        entity.setUserId(log.getUserId());
        entity.setAction(log.getAction());
        entity.setEntity(log.getEntity());
        entity.setEntityId(log.getEntityId());
        entity.setDetail(log.getDetail());
        entity.setIpAddress(log.getIpAddress());
        entity.setOccurredAt(log.getOccurredAt());
        return entity;
    }
}
