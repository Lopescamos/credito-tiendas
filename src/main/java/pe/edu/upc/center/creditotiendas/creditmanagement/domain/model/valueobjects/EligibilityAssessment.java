package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.util.List;

/**
 * Outcome of checking the US-08 eligibility rules. Lists every failed rule, not only the first,
 * so the customer sees everything that blocks the request.
 */
public record EligibilityAssessment(List<EligibilityFailures> failures) {

    public EligibilityAssessment {
        failures = List.copyOf(failures);
    }

    public boolean eligible() {
        return failures.isEmpty();
    }
}
