package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.util.List;

/**
 * @param failures every US-08 rule the customer does not meet; empty when eligible
 */
public record PurchaseRequestEligibilityResource(boolean eligible, List<String> failures) {
}
