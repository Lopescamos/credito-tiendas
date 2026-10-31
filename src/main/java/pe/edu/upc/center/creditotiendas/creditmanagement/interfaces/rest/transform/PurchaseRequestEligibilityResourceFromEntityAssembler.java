package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityAssessment;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.PurchaseRequestEligibilityResource;

public class PurchaseRequestEligibilityResourceFromEntityAssembler {
    public static PurchaseRequestEligibilityResource toResourceFromEntity(EligibilityAssessment assessment) {
        return new PurchaseRequestEligibilityResource(
                assessment.eligible(),
                assessment.failures().stream().map(Enum::name).toList());
    }
}
