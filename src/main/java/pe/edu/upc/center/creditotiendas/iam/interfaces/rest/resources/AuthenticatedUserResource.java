package pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources;

import java.util.List;

/**
 * Sign-in response. {@code storeIds} lets a store admin pick the store to work on.
 */
public record AuthenticatedUserResource(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        List<Long> storeIds,
        String token) {
}
