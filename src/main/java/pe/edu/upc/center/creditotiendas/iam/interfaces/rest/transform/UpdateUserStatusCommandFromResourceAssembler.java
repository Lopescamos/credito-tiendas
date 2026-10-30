package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.UpdateUserStatusCommand;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.UpdateUserStatusResource;

public class UpdateUserStatusCommandFromResourceAssembler {
    public static UpdateUserStatusCommand toCommandFromResource(Long userId, UpdateUserStatusResource resource) {
        return new UpdateUserStatusCommand(userId, resource.active());
    }
}
