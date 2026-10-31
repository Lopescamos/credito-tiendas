package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreateCreditProfileResource;

public class CreateCreditProfileCommandFromResourceAssembler {
    public static CreateCreditProfileCommand toCommandFromResource(Long storeId, CreateCreditProfileResource resource) {
        return new CreateCreditProfileCommand(
                storeId,
                resource.clientEmail(),
                new InterestRate(resource.interestRate(), resource.rateType(), resource.capitalizationPeriod()),
                new LateInterestRate(resource.lateInterestRate(), resource.lateInterestRateType()),
                resource.currencyId(),
                resource.creditLimit(),
                resource.maxTermDays(),
                resource.cutoffDay(),
                resource.paymentDay());
    }
}
