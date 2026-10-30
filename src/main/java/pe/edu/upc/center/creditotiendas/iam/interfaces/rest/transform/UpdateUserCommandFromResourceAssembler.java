package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {
    public static UpdateUserCommand toCommandFromResource(Long userId, UpdateUserResource resource) {
        return new UpdateUserCommand(userId, resource.firstName(), resource.lastName());
    }
}
