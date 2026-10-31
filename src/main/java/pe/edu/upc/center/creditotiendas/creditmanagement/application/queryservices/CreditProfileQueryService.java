package pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityAssessment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CreditProfileQueryService {
    Optional<CreditProfile> handle(GetCreditProfileByIdQuery query);
    List<CreditProfile> handle(GetCreditProfilesByStoreIdQuery query);
    List<CreditProfile> handle(GetCreditProfilesByUserEmailQuery query);
    Optional<BigDecimal> handle(GetAvailableCreditByCreditProfileIdQuery query);
    Optional<EligibilityAssessment> handle(GetPurchaseRequestEligibilityQuery query);
}
