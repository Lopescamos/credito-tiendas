package pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.ApproveCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.RejectCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

public interface CreditPurchaseRequestCommandService {
    Result<Long, ApplicationError> handle(CreateCreditPurchaseRequestCommand command);
    Result<CreditPurchaseRequest, ApplicationError> handle(ApproveCreditPurchaseRequestCommand command);
    Result<CreditPurchaseRequest, ApplicationError> handle(RejectCreditPurchaseRequestCommand command);
}
