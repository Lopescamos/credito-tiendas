package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.queryservices;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.ProductQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetActiveProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.ProductRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.caching.CatalogCacheNames;

import java.util.List;
import java.util.Optional;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    @Override
    public List<Product> handle(GetAllProductsByStoreIdQuery query) {
        return productRepository.findAllByStoreId(query.storeId());
    }

    /**
     * Cached (US-08 catalog, high traffic): a write that affects this list evicts the entry for
     * its {@code storeId} — see {@code ProductCommandServiceImpl}.
     */
    @Override
    @Cacheable(cacheNames = CatalogCacheNames.ACTIVE_PRODUCTS_BY_STORE, key = "#query.storeId()")
    public List<Product> handle(GetActiveProductsByStoreIdQuery query) {
        return productRepository.findAllActiveByStoreId(query.storeId());
    }
}
