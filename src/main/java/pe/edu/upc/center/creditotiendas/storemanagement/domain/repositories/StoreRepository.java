package pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link Store} aggregate.
 */
public interface StoreRepository {
    Optional<Store> findById(Long id);
    List<Store> findAll();
    boolean existsByRuc(Ruc ruc);
    Store save(Store store);
}
