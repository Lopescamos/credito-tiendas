package pe.edu.upc.center.creditotiendas.billing.application.commandservices;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

public interface PaymentCommandService {
    Result<Payment, ApplicationError> handle(RegisterPaymentCommand command);
}
