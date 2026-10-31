package pe.edu.upc.center.creditotiendas.creditmanagement.domain.services;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CreditSaleStatuses;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityAssessment;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityFailures;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * US-08 eligibility for a remote credit purchase request. All rules are mandatory:
 * <ol>
 *   <li>the credit profile is active;</li>
 *   <li>at least one previous sale was paid on time;</li>
 *   <li>no sale has overdue unpaid installments;</li>
 *   <li>the requested amount fits in the available credit.</li>
 * </ol>
 *
 * <p>Rules 2 and 3 read the sale statuses that Billing maintains, so Credit Management does not need to
 * query Billing.</p>
 */
public final class PurchaseRequestEligibilityPolicy {

    private PurchaseRequestEligibilityPolicy() {
    }

    /**
     * @param requestedAmount amount to request; {@code null} to check only whether the customer may
     *                        request at all (rule 4 is skipped)
     */
    public static EligibilityAssessment assess(boolean profileActive, Collection<CreditSaleStatuses> saleStatuses,
                                               BigDecimal availableCredit, BigDecimal requestedAmount) {
        var failures = new ArrayList<EligibilityFailures>();
        if (!profileActive)
            failures.add(EligibilityFailures.PROFILE_INACTIVE);
        if (!saleStatuses.contains(CreditSaleStatuses.PAID_ON_TIME))
            failures.add(EligibilityFailures.NO_SALE_PAID_ON_TIME);
        if (saleStatuses.contains(CreditSaleStatuses.OVERDUE))
            failures.add(EligibilityFailures.HAS_OVERDUE_INSTALLMENTS);
        if (requestedAmount != null && requestedAmount.compareTo(availableCredit) > 0)
            failures.add(EligibilityFailures.AMOUNT_EXCEEDS_AVAILABLE_CREDIT);
        return new EligibilityAssessment(failures);
    }
}
