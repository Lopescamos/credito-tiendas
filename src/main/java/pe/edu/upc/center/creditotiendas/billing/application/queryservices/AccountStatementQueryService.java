package pe.edu.upc.center.creditotiendas.billing.application.queryservices;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetAccountStatementByIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetAccountStatementsByCreditProfileIdQuery;

import java.util.List;
import java.util.Optional;

public interface AccountStatementQueryService {
    Optional<AccountStatement> handle(GetAccountStatementByIdQuery query);
    List<AccountStatement> handle(GetAccountStatementsByCreditProfileIdQuery query);
}
