package pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.adapters;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.center.creditotiendas.audit.domain.model.queries.GetOperationLogsQuery;
import pe.edu.upc.center.creditotiendas.audit.domain.repositories.OperationLogRepository;
import pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.assemblers.OperationLogPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.repositories.OperationLogPersistenceRepository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OperationLogRepositoryImpl implements OperationLogRepository {

    private final OperationLogPersistenceRepository persistenceRepository;

    public OperationLogRepositoryImpl(OperationLogPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public OperationLog save(OperationLog operationLog) {
        var saved = persistenceRepository.save(
                OperationLogPersistenceAssembler.toPersistenceFromDomain(operationLog));
        return OperationLogPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public List<OperationLog> search(GetOperationLogsQuery query) {
        Specification<pe.edu.upc.center.creditotiendas.audit.infrastructure.persistence.jpa.entities.OperationLogPersistenceEntity>
                spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.userId() != null) predicates.add(cb.equal(root.get("userId"), query.userId()));
            if (query.action() != null) predicates.add(cb.equal(root.get("action"), query.action()));
            if (query.entity() != null && !query.entity().isBlank())
                predicates.add(cb.equal(cb.lower(root.get("entity")), query.entity().toLowerCase()));
            if (query.from() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), query.from()));
            if (query.to() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), query.to()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return persistenceRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "occurredAt")).stream()
                .map(OperationLogPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
