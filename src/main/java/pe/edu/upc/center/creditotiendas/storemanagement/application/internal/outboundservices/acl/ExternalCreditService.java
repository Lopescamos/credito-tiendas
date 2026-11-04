package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl.CreditManagementContextFacade;

/**
 * Store Management's view of Credit Management: whether a client is actually a customer of a
 * store (has an active credit profile there), used to restrict the catalog a client can see.
 *
 * <p>Explicit bean name: Billing has its own {@code ExternalCreditService}.</p>
 */
@Service("storeManagementExternalCreditService")
public class ExternalCreditService {

    private final CreditManagementContextFacade creditManagementContextFacade;

    public ExternalCreditService(CreditManagementContextFacade creditManagementContextFacade) {
        this.creditManagementContextFacade = creditManagementContextFacade;
    }

    public boolean hasActiveProfile(Long userId, Long storeId) {
        return creditManagementContextFacade.hasActiveProfile(userId, storeId);
    }
}
