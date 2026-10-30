package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.CreateUserCommand;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.CreateUserResource;

public class CreateUserCommandFromResourceAssembler {
    public static CreateUserCommand toCommandFromResource(CreateUserResource resource) {
        return new CreateUserCommand(
                resource.email(), resource.firstName(), resource.lastName(), resource.password(), resource.role());
    }
}
