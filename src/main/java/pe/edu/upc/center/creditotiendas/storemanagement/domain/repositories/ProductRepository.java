package pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link Product} aggregate.
 */
public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAllByStoreId(Long storeId);
    List<Product> findAllActiveByStoreId(Long storeId);
    Product save(Product product);
}
