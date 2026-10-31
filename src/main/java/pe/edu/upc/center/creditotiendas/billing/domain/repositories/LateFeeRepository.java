package pe.edu.upc.center.creditotiendas.billing.domain.repositories;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.LateFee;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Persistence port of the {@link LateFee} aggregate.
 */
public interface LateFeeRepository {
    LateFee save(LateFee lateFee);
    List<LateFee> findAllByInstallmentId(Long installmentId);
    /**
     * Sum of the newest {@link LateFee} per installment whose owning statement was computed in the given
     * period. Used to build monthly statements.
     */
    BigDecimal sumLatestByCreditProfileIdInRange(Long creditProfileId, Instant from, Instant to);
}
