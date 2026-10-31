package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries;

/**
 * Every credit profile of a customer, one per store (US-03). The email is the authenticated user.
 */
public record GetCreditProfilesByUserEmailQuery(String email) {
}
