package pe.edu.upc.center.creditotiendas.iam.application.commandservices;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;

/**
 * Result of a successful sign-in: the user and the access token issued for them.
 */
public record AuthenticatedUser(User user, String token) {
}
