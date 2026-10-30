package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.ProductRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers.ProductPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.repositories.ProductPersistenceRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductPersistenceRepository productPersistenceRepository;

    public ProductRepositoryImpl(ProductPersistenceRepository productPersistenceRepository) {
        this.productPersistenceRepository = productPersistenceRepository;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productPersistenceRepository.findById(id).map(ProductPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Product> findAllByStoreId(Long storeId) {
        return productPersistenceRepository.findAllByStoreIdOrderByNameAsc(storeId).stream()
                .map(ProductPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Product> findAllActiveByStoreId(Long storeId) {
        return productPersistenceRepository.findAllByStoreIdAndActiveTrueOrderByNameAsc(storeId).stream()
                .map(ProductPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Product save(Product product) {
        var saved = productPersistenceRepository.save(ProductPersistenceAssembler.toPersistenceFromDomain(product));
        return ProductPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
