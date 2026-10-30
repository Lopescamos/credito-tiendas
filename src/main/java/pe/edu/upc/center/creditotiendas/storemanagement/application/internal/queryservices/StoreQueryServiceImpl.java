package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.StoreQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllStoresQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.StoreRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StoreQueryServiceImpl implements StoreQueryService {

    private final StoreRepository storeRepository;

    public StoreQueryServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public Optional<Store> handle(GetStoreByIdQuery query) {
        return storeRepository.findById(query.storeId());
    }

    @Override
    public List<Store> handle(GetAllStoresQuery query) {
        return storeRepository.findAll();
    }
}
