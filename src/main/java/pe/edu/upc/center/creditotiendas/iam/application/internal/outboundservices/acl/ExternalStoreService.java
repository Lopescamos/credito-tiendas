package pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.acl.StoreManagementContextFacade;

/**
 * IAM's view of Store Management: whether a store can receive newly linked users (US-13).
 */
@Service
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
}
