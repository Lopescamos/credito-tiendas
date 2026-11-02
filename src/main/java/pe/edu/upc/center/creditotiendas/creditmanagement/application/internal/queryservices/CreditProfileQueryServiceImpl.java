package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalBillingService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalIamService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditProfileQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.*;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.EligibilityAssessment;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditPurchaseRequestRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.PurchaseRequestEligibilityPolicy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CreditProfileQueryServiceImpl implements CreditProfileQueryService {

    private final CreditProfileRepository creditProfileRepository;
    private final CreditSaleRepository creditSaleRepository;
    private final CreditPurchaseRequestRepository purchaseRequestRepository;
    private final ExternalIamService externalIamService;
    private final ExternalBillingService externalBillingService;

    public CreditProfileQueryServiceImpl(CreditProfileRepository creditProfileRepository,
                                         CreditSaleRepository creditSaleRepository,
                                         CreditPurchaseRequestRepository purchaseRequestRepository,
                                         ExternalIamService externalIamService,
                                         ExternalBillingService externalBillingService) {
        this.creditProfileRepository = creditProfileRepository;
        this.creditSaleRepository = creditSaleRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.externalIamService = externalIamService;
        this.externalBillingService = externalBillingService;
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
                .map(profile -> profile.availableCredit(
                        externalBillingService.fetchPendingCapital(profile.getId()),
                        reservedCreditFor(profile.getId())));
    }

    /**
     * Checks every US-08 rule except the amount, which is unknown until the customer picks products.
     */
    @Override
    public Optional<EligibilityAssessment> handle(GetPurchaseRequestEligibilityQuery query) {
        return creditProfileRepository.findById(query.creditProfileId()).map(profile -> {
            var sales = creditSaleRepository.findAllByCreditProfileId(profile.getId());
            var availableCredit = profile.availableCredit(
                    externalBillingService.fetchPendingCapital(profile.getId()),
                    reservedCreditFor(profile.getId()));
            return PurchaseRequestEligibilityPolicy.assess(
                    profile.isActive(),
                    sales.stream().map(CreditSale::getStatus).toList(),
                    availableCredit,
                    null);
        });
    }

    /**
     * Sum of the total amount of this profile's pending purchase requests, which also commit part of the
     * credit limit even before they become a sale.
     */
    private BigDecimal reservedCreditFor(Long creditProfileId) {
        return purchaseRequestRepository.findAllByCreditProfileId(creditProfileId).stream()
                .filter(CreditPurchaseRequest::isPending)
                .map(CreditPurchaseRequest::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
