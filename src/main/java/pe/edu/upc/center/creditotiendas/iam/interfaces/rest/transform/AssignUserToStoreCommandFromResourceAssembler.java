package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.AssignUserToStoreCommand;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.AssignUserToStoreResource;

public class AssignUserToStoreCommandFromResourceAssembler {
    public static AssignUserToStoreCommand toCommandFromResource(Long userId, AssignUserToStoreResource resource) {
        return new AssignUserToStoreCommand(userId, resource.storeId());
    }
}
