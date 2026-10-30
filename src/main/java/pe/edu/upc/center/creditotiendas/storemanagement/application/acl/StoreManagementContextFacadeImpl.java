package pe.edu.upc.center.creditotiendas.storemanagement.application.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.StoreQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.acl.StoreManagementContextFacade;

@Service
public class StoreManagementContextFacadeImpl implements StoreManagementContextFacade {

    private final StoreQueryService storeQueryService;

    public StoreManagementContextFacadeImpl(StoreQueryService storeQueryService) {
        this.storeQueryService = storeQueryService;
    }

    @Override
    public boolean existsStoreById(Long storeId) {
        return storeQueryService.handle(new GetStoreByIdQuery(storeId)).isPresent();
    }

    @Override
    public boolean isStoreActive(Long storeId) {
        return storeQueryService.handle(new GetStoreByIdQuery(storeId))
                .map(Store::isActive)
                .orElse(false);
    }
}
