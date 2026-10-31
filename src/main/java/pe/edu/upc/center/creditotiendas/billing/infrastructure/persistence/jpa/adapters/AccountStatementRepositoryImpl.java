package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.AccountStatementRepository;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers.AccountStatementPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories.AccountStatementPersistenceRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountStatementRepositoryImpl implements AccountStatementRepository {

    private final AccountStatementPersistenceRepository persistenceRepository;

    public AccountStatementRepositoryImpl(AccountStatementPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<AccountStatement> findById(Long id) {
        return persistenceRepository.findById(id).map(AccountStatementPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<AccountStatement> findLatestByCreditProfileIdBefore(Long creditProfileId, YearMonth period) {
        return persistenceRepository.findFirstByCreditProfileIdAndPeriodLessThanOrderByPeriodDesc(
                creditProfileId, period.toString()).map(AccountStatementPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<AccountStatement> findAllByCreditProfileId(Long creditProfileId) {
        return persistenceRepository.findAllByCreditProfileIdOrderByPeriodDesc(creditProfileId).stream()
                .map(AccountStatementPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByCreditProfileIdAndPeriod(Long creditProfileId, YearMonth period) {
        return persistenceRepository.existsByCreditProfileIdAndPeriod(creditProfileId, period.toString());
    }

    @Override
    public AccountStatement save(AccountStatement statement) {
        var saved = persistenceRepository.save(AccountStatementPersistenceAssembler.toPersistenceFromDomain(statement));
        return AccountStatementPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
