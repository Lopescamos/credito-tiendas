package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.interfaces.acl.BillingContextFacade;

import java.math.BigDecimal;

/**
 * Credit Management's view of Billing: the capital still owed on unpaid installments, used to compute
 * available credit (v2: credit limit minus capital still financed, not the total of outstanding sales).
 */
@Service
public class ExternalBillingService {

    private final BillingContextFacade billingContextFacade;

    public ExternalBillingService(BillingContextFacade billingContextFacade) {
        this.billingContextFacade = billingContextFacade;
    }

    public BigDecimal fetchPendingCapital(Long creditProfileId) {
        return billingContextFacade.fetchPendingCapitalByCreditProfileId(creditProfileId);
    }
}
