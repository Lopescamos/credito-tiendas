package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.StoreRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers.StorePersistenceAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories.StorePersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class StoreRepositoryImpl implements StoreRepository {

    private final StorePersistenceRepository storePersistenceRepository;

    public StoreRepositoryImpl(StorePersistenceRepository storePersistenceRepository) {
        this.storePersistenceRepository = storePersistenceRepository;
    }

    @Override
    public Optional<Store> findById(Long id) {
        return storePersistenceRepository.findById(id).map(StorePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Store> findAll() {
        return storePersistenceRepository.findAll().stream()
                .map(StorePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByRuc(Ruc ruc) {
        return storePersistenceRepository.existsByRuc(ruc);
    }

    @Override
    public Store save(Store store) {
        var saved = storePersistenceRepository.save(StorePersistenceAssembler.toPersistenceFromDomain(store));
        return StorePersistenceAssembler.toDomainFromPersistence(saved);
    }
}
