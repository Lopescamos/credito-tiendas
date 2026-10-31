package pe.edu.upc.center.creditotiendas.storemanagement.application.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.CurrencyQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.ProductQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.StoreQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetCurrencyByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.acl.StoreManagementContextFacade;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class StoreManagementContextFacadeImpl implements StoreManagementContextFacade {

    private final StoreQueryService storeQueryService;
    private final CurrencyQueryService currencyQueryService;
    private final ProductQueryService productQueryService;

    public StoreManagementContextFacadeImpl(StoreQueryService storeQueryService,
                                            CurrencyQueryService currencyQueryService,
                                            ProductQueryService productQueryService) {
        this.storeQueryService = storeQueryService;
        this.currencyQueryService = currencyQueryService;
        this.productQueryService = productQueryService;
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

    @Override
    public boolean existsCurrencyById(Long currencyId) {
        return currencyQueryService.handle(new GetCurrencyByIdQuery(currencyId)).isPresent();
    }

    @Override
    public BigDecimal fetchAvailableProductCreditPrice(Long productId, Long storeId) {
        return productQueryService.handle(new GetProductByIdQuery(productId))
                .filter(product -> Objects.equals(product.getStoreId(), storeId) && product.isActive())
                .map(Product::getCreditPrice)
                .orElse(BigDecimal.ZERO);
    }
}
