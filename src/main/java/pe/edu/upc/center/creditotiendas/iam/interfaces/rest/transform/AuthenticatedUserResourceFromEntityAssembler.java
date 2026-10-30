package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.iam.application.commandservices.AuthenticatedUser;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(AuthenticatedUser authenticatedUser) {
        var user = authenticatedUser.user();
        return new AuthenticatedUserResource(
                user.getId(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getActiveStoreIds(),
                authenticatedUser.token());
    }
}
