package pe.edu.upc.center.creditotiendas.billing.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.AccountStatementQueryService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetAccountStatementByIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetAccountStatementsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.AccountStatementRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AccountStatementQueryServiceImpl implements AccountStatementQueryService {

    private final AccountStatementRepository accountStatementRepository;

    public AccountStatementQueryServiceImpl(AccountStatementRepository accountStatementRepository) {
        this.accountStatementRepository = accountStatementRepository;
    }

    @Override
    public Optional<AccountStatement> handle(GetAccountStatementByIdQuery query) {
        return accountStatementRepository.findById(query.accountStatementId());
    }

    @Override
    public List<AccountStatement> handle(GetAccountStatementsByCreditProfileIdQuery query) {
        return accountStatementRepository.findAllByCreditProfileId(query.creditProfileId());
    }
}
