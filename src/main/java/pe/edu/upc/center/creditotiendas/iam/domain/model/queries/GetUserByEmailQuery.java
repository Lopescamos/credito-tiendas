package pe.edu.upc.center.creditotiendas.iam.domain.model.queries;

import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;

/**
 * Used to resolve the authenticated user from the email stored as JWT subject.
 */
public record GetUserByEmailQuery(EmailAddress email) {
}
