package pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetActiveProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ProductQueryService {
    Optional<Product> handle(GetProductByIdQuery query);
    List<Product> handle(GetAllProductsByStoreIdQuery query);
    List<Product> handle(GetActiveProductsByStoreIdQuery query);
}
