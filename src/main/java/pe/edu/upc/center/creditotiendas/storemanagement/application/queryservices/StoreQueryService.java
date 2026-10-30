package pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllStoresQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetStoreByIdQuery;

import java.util.List;
import java.util.Optional;

public interface StoreQueryService {
    Optional<Store> handle(GetStoreByIdQuery query);
    List<Store> handle(GetAllStoresQuery query);
}
