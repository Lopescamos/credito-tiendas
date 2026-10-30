package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import java.util.List;

/**
 * User as returned by the API. Never includes the password hash.
 */
public record UserResource(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean active,
        List<Long> storeIds) {
}
