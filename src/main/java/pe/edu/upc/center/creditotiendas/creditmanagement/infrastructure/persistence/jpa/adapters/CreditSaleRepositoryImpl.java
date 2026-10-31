package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers.CreditSalePersistenceAssembler;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditSalePersistenceEntity;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.repositories.CreditSalePersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class CreditSaleRepositoryImpl implements CreditSaleRepository {

    private final CreditSalePersistenceRepository persistenceRepository;

    public CreditSaleRepositoryImpl(CreditSalePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<CreditSale> findById(Long id) {
        return persistenceRepository.findById(id).map(CreditSalePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<CreditSale> findAllByCreditProfileId(Long creditProfileId) {
        return toDomain(persistenceRepository.findAllByCreditProfileIdOrderBySaleDateDescIdDesc(creditProfileId));
    }

    @Override
    public CreditSale save(CreditSale aggregate) {
        var saved = persistenceRepository.save(CreditSalePersistenceAssembler.toPersistenceFromDomain(aggregate));
        return CreditSalePersistenceAssembler.toDomainFromPersistence(saved);
    }

    private static List<CreditSale> toDomain(List<CreditSalePersistenceEntity> entities) {
        return entities.stream().map(CreditSalePersistenceAssembler::toDomainFromPersistence).toList();
    }
}
