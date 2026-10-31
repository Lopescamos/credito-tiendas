package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditProfileResource;

import java.math.BigDecimal;

public class CreditProfileResourceFromEntityAssembler {
    public static CreditProfileResource toResourceFromEntity(CreditProfile profile, BigDecimal availableCredit) {
        var rate = profile.getInterestRate();
        var lateRate = profile.getLateInterestRate();
        return new CreditProfileResource(
                profile.getId(),
                profile.getUserId(),
                profile.getStoreId(),
                rate.percentage(),
                rate.type().name(),
                rate.capitalizationPeriod() == null ? null : rate.capitalizationPeriod().name(),
                lateRate.percentage(),
                lateRate.type().name(),
                profile.getCurrencyId(),
                profile.getCreditLimit(),
                availableCredit,
                profile.getMaxTermDays(),
                profile.getCutoffDay(),
                profile.getPaymentDay(),
                profile.isActive());
    }
}
