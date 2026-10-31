package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.acl.StoreManagementContextFacade;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Credit Management's view of Store Management: active stores, currencies and sellable catalog items.
 *
 * <p>Explicit bean name: IAM has its own {@code ExternalStoreService}.</p>
 */
@Service("creditManagementExternalStoreService")
public class ExternalStoreService {

    private final StoreManagementContextFacade storeManagementContextFacade;

    public ExternalStoreService(StoreManagementContextFacade storeManagementContextFacade) {
        this.storeManagementContextFacade = storeManagementContextFacade;
    }

    public boolean existsStore(Long storeId) {
        return storeManagementContextFacade.existsStoreById(storeId);
    }

    public boolean isStoreActive(Long storeId) {
        return storeManagementContextFacade.isStoreActive(storeId);
    }

    public boolean existsCurrency(Long currencyId) {
        return storeManagementContextFacade.existsCurrencyById(currencyId);
    }

    /**
     * @return the credit price, or empty if the product cannot be sold now in that store
     */
    public Optional<BigDecimal> fetchAvailableProductCreditPrice(Long productId, Long storeId) {
        var price = storeManagementContextFacade.fetchAvailableProductCreditPrice(productId, storeId);
        return price.signum() > 0 ? Optional.of(price) : Optional.empty();
    }
}
