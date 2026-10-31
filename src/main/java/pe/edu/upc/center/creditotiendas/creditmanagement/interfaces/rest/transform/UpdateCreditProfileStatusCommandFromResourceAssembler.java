package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.UpdateCreditProfileStatusCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.UpdateCreditProfileStatusResource;

public class UpdateCreditProfileStatusCommandFromResourceAssembler {
    public static UpdateCreditProfileStatusCommand toCommandFromResource(Long creditProfileId,
                                                                         UpdateCreditProfileStatusResource resource) {
        return new UpdateCreditProfileStatusCommand(creditProfileId, resource.active());
    }
}
