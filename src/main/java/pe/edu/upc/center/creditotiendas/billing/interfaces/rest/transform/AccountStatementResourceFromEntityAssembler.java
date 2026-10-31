package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.AccountStatementDetailResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.AccountStatementResource;

public class AccountStatementResourceFromEntityAssembler {
    public static AccountStatementResource toResourceFromEntity(AccountStatement statement) {
        return new AccountStatementResource(
                statement.getId(), statement.getCreditProfileId(), statement.getPeriod().toString(),
                statement.getCutoffDate(), statement.getPreviousBalance(), statement.getPeriodPurchases(),
                statement.getPeriodPayments(), statement.getPeriodInterest(), statement.getPeriodLateFees(),
                statement.getFinalBalance(),
                statement.getDetails().stream()
                        .map(d -> new AccountStatementDetailResource(d.getId(),
                                d.getMovementType().name(), d.getDescription(), d.getAmount(), d.getDate()))
                        .toList());
    }
}
