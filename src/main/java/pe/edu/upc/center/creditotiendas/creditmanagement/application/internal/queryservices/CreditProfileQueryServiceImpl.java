package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalIamService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditProfileQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityAssessment;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.PurchaseRequestEligibilityPolicy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CreditProfileQueryServiceImpl implements CreditProfileQueryService {

    private final CreditProfileRepository creditProfileRepository;
    private final CreditSaleRepository creditSaleRepository;
    private final ExternalIamService externalIamService;

    public CreditProfileQueryServiceImpl(CreditProfileRepository creditProfileRepository,
                                         CreditSaleRepository creditSaleRepository,
                                         ExternalIamService externalIamService) {
        this.creditProfileRepository = creditProfileRepository;
        this.creditSaleRepository = creditSaleRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<CreditProfile> handle(GetCreditProfileByIdQuery query) {
        return creditProfileRepository.findById(query.creditProfileId());
    }

    @Override
    public List<CreditProfile> handle(GetCreditProfilesByStoreIdQuery query) {
        return creditProfileRepository.findAllByStoreId(query.storeId());
    }

    @Override
    public List<CreditProfile> handle(GetCreditProfilesByUserEmailQuery query) {
        return externalIamService.fetchUserIdByEmail(query.email())
                .map(creditProfileRepository::findAllByUserId)
                .orElse(List.of());
    }

    @Override
    public Optional<BigDecimal> handle(GetAvailableCreditByCreditProfileIdQuery query) {
        return creditProfileRepository.findById(query.creditProfileId())
                .map(profile -> profile.availableCredit(creditSaleRepository.findAllByCreditProfileId(profile.getId())));
    }

    /**
     * Checks every US-08 rule except the amount, which is unknown until the customer picks products.
     */
    @Override
    public Optional<EligibilityAssessment> handle(GetPurchaseRequestEligibilityQuery query) {
        return creditProfileRepository.findById(query.creditProfileId()).map(profile -> {
            var sales = creditSaleRepository.findAllByCreditProfileId(profile.getId());
            return PurchaseRequestEligibilityPolicy.assess(
                    profile.isActive(),
                    sales.stream().map(CreditSale::getStatus).toList(),
                    profile.availableCredit(sales),
                    null);
        });
    }
}
