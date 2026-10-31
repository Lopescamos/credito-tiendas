package pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.RegisterPaymentResource;

public class RegisterPaymentCommandFromResourceAssembler {
    public static RegisterPaymentCommand toCommandFromResource(Long creditProfileId, RegisterPaymentResource resource) {
        return new RegisterPaymentCommand(creditProfileId, resource.installmentIds(),
                resource.paymentDate(), resource.paymentMethod(), resource.reference());
    }
}
