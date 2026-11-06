package pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.entities.OperationLogPersistenceEntity;

public interface OperationLogPersistenceRepository extends
        JpaRepository<OperationLogPersistenceEntity, Long>,
        JpaSpecificationExecutor<OperationLogPersistenceEntity> {
}
