package pe.edu.upc.center.creditotiendas.billing.application.commandservices;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.GenerateAccountStatementCommand;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

public interface AccountStatementCommandService {
    Result<AccountStatement, ApplicationError> handle(GenerateAccountStatementCommand command);
}
