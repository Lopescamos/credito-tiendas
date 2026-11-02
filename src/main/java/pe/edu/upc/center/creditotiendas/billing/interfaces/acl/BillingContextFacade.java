package pe.edu.upc.center.creditotiendas.billing.interfaces.acl;

import java.math.BigDecimal;

/**
 * Public face of the Billing context for other bounded contexts.
 */
public interface BillingContextFacade {

    /**
     * @return the sum of the capital still owed on every unpaid (pending or overdue) installment of this
     *         credit profile, across all its sales. Zero if the profile has no unpaid installments or
     *         does not exist. Used by Credit Management to compute the real available credit (the capital
     *         still financed, not the total of outstanding sales).
     */
    BigDecimal fetchPendingCapitalByCreditProfileId(Long creditProfileId);
}
