package pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.UpdateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.UpdateCreditProfileStatusCommand;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

public interface CreditProfileCommandService {
    Result<Long, ApplicationError> handle(CreateCreditProfileCommand command);
    Result<CreditProfile, ApplicationError> handle(UpdateCreditProfileCommand command);
    Result<CreditProfile, ApplicationError> handle(UpdateCreditProfileStatusCommand command);
}
