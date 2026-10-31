package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers.CreditProfilePersistenceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditProfilePersistenceEntity;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.repositories.CreditProfilePersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class CreditProfileRepositoryImpl implements CreditProfileRepository {

    private final CreditProfilePersistenceRepository persistenceRepository;

    public CreditProfileRepositoryImpl(CreditProfilePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<CreditProfile> findById(Long id) {
        return persistenceRepository.findById(id).map(CreditProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<CreditProfile> findAllByStoreId(Long storeId) {
        return toDomain(persistenceRepository.findAllByStoreIdOrderByIdAsc(storeId));
    }

    @Override
    public List<CreditProfile> findAllByUserId(Long userId) {
        return toDomain(persistenceRepository.findAllByUserIdOrderByIdAsc(userId));
    }

    @Override
    public boolean existsByUserIdAndStoreId(Long userId, Long storeId) {
        return persistenceRepository.existsByUserIdAndStoreId(userId, storeId);
    }

    @Override
    public CreditProfile save(CreditProfile aggregate) {
        var saved = persistenceRepository.save(CreditProfilePersistenceAssembler.toPersistenceFromDomain(aggregate));
        return CreditProfilePersistenceAssembler.toDomainFromPersistence(saved);
    }

    private static List<CreditProfile> toDomain(List<CreditProfilePersistenceEntity> entities) {
        return entities.stream().map(CreditProfilePersistenceAssembler::toDomainFromPersistence).toList();
    }
}
