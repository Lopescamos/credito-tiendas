package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.ProductQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetActiveProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.ProductRepository;

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

    @Override
    public List<Product> handle(GetActiveProductsByStoreIdQuery query) {
        return productRepository.findAllActiveByStoreId(query.storeId());
    }
}
