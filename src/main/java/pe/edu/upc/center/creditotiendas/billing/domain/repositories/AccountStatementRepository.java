package pe.edu.upc.center.creditotiendas.billing.domain.repositories;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link AccountStatement} aggregate.
 */
public interface AccountStatementRepository {
    Optional<AccountStatement> findById(Long id);
    Optional<AccountStatement> findLatestByCreditProfileIdBefore(Long creditProfileId, YearMonth period);
    List<AccountStatement> findAllByCreditProfileId(Long creditProfileId);
    boolean existsByCreditProfileIdAndPeriod(Long creditProfileId, YearMonth period);
    AccountStatement save(AccountStatement statement);
}
