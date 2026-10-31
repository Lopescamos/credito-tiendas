package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.acl;

import java.util.List;
import java.util.Optional;

/**
 * Public face of the Credit Management context for other bounded contexts. Primitive types and record
 * summaries only so the Credit model never leaks.
 */
public interface CreditManagementContextFacade {

    Optional<CreditProfileSummary> fetchProfileById(Long creditProfileId);

    /**
     * @return ids of every active credit profile whose cut-off day equals {@code cutoffDay} (used by Billing's
     *         scheduler to generate monthly statements)
     */
    List<Long> fetchActiveProfileIdsByCutoffDay(int cutoffDay);

    /**
     * Updates the status of a credit sale when Billing detects an installment going overdue or the sale being
     * fully paid. Allowed transitions:
     * <ul>
     *   <li>{@code ACTIVE} → {@code OVERDUE} when an installment turns overdue.</li>
     *   <li>Any status → {@code PAID_ON_TIME} / {@code PAID_LATE} when the last installment is paid.</li>
     *   <li>{@code OVERDUE} → {@code ACTIVE} when every overdue installment is paid and some are still due.</li>
     * </ul>
     *
     * @param newStatus one of {@code ACTIVE}, {@code OVERDUE}, {@code PAID_ON_TIME}, {@code PAID_LATE}
     * @return {@code true} if the status was updated, {@code false} if the sale does not exist or the status
     *         name is unknown
     */
    boolean updateCreditSaleStatus(Long creditSaleId, String newStatus);
}
