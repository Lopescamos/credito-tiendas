package pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.RegisterCreditSaleCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.SimulateCreditSaleCommand;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

public interface CreditSaleCommandService {
    Result<Long, ApplicationError> handle(RegisterCreditSaleCommand command);

    /**
     * Applies exactly the same rules as registering, but returns the unsaved sale with its plan.
     */
    Result<CreditSale, ApplicationError> handle(SimulateCreditSaleCommand command);
}
