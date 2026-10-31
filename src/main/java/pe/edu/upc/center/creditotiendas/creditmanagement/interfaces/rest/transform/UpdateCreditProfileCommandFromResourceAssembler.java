package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.UpdateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.UpdateCreditProfileResource;

public class UpdateCreditProfileCommandFromResourceAssembler {
    public static UpdateCreditProfileCommand toCommandFromResource(Long creditProfileId, UpdateCreditProfileResource resource) {
        return new UpdateCreditProfileCommand(
                creditProfileId,
                new InterestRate(resource.interestRate(), resource.rateType(), resource.capitalizationPeriod()),
                new LateInterestRate(resource.lateInterestRate(), resource.lateInterestRateType()),
                resource.currencyId(),
                resource.creditLimit(),
                resource.maxTermDays(),
                resource.cutoffDay(),
                resource.paymentDay());
    }
}
