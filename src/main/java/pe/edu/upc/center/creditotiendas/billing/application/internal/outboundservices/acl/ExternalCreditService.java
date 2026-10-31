package pe.edu.upc.center.creditotiendas.billing.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl.CreditManagementContextFacade;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl.CreditProfileSummary;

import java.util.List;
import java.util.Optional;

/**
 * Billing's view of Credit: profile summaries (for statements and the scheduler) and status updates on sales.
 */
@Service
public class ExternalCreditService {

    private final CreditManagementContextFacade creditManagementContextFacade;

    public ExternalCreditService(CreditManagementContextFacade creditManagementContextFacade) {
        this.creditManagementContextFacade = creditManagementContextFacade;
    }

    public Optional<CreditProfileSummary> fetchProfile(Long creditProfileId) {
        return creditManagementContextFacade.fetchProfileById(creditProfileId);
    }

    public List<Long> fetchActiveProfileIdsByCutoffDay(int cutoffDay) {
        return creditManagementContextFacade.fetchActiveProfileIdsByCutoffDay(cutoffDay);
    }

    public void markCreditSaleAsOverdue(Long creditSaleId) {
        creditManagementContextFacade.updateCreditSaleStatus(creditSaleId, "OVERDUE");
    }

    public void markCreditSaleAsPaidOnTime(Long creditSaleId) {
        creditManagementContextFacade.updateCreditSaleStatus(creditSaleId, "PAID_ON_TIME");
    }

    public void markCreditSaleAsPaidLate(Long creditSaleId) {
        creditManagementContextFacade.updateCreditSaleStatus(creditSaleId, "PAID_LATE");
    }
}
