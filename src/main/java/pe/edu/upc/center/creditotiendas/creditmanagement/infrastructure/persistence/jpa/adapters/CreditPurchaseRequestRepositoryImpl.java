package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditPurchaseRequestRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers.CreditPurchaseRequestPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditPurchaseRequestPersistenceEntity;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.repositories.CreditPurchaseRequestPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class CreditPurchaseRequestRepositoryImpl implements CreditPurchaseRequestRepository {

    private final CreditPurchaseRequestPersistenceRepository persistenceRepository;

    public CreditPurchaseRequestRepositoryImpl(CreditPurchaseRequestPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<CreditPurchaseRequest> findById(Long id) {
        return persistenceRepository.findById(id).map(CreditPurchaseRequestPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<CreditPurchaseRequest> findAllByCreditProfileId(Long creditProfileId) {
        return toDomain(persistenceRepository.findAllByCreditProfileIdOrderByRequestedAtDesc(creditProfileId));
    }

    @Override
    public List<CreditPurchaseRequest> findAllByStoreId(Long storeId) {
        return toDomain(persistenceRepository.findAllByStoreId(storeId));
    }

    @Override
    public CreditPurchaseRequest save(CreditPurchaseRequest aggregate) {
        var saved = persistenceRepository.save(CreditPurchaseRequestPersistenceAssembler.toPersistenceFromDomain(aggregate));
        return CreditPurchaseRequestPersistenceAssembler.toDomainFromPersistence(saved);
    }

    private static List<CreditPurchaseRequest> toDomain(List<CreditPurchaseRequestPersistenceEntity> entities) {
        return entities.stream().map(CreditPurchaseRequestPersistenceAssembler::toDomainFromPersistence).toList();
    }
}
