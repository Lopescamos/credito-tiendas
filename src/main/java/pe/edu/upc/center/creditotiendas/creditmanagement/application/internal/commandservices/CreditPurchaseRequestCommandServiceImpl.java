package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditPurchaseRequestCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalStoreService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.ApproveCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.RejectCreditPurchaseRequestCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.PurchaseRequestDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.SaleModalities;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditPurchaseRequestRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.PurchaseRequestEligibilityPolicy;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Remote credit purchase requests: sent by customers (US-08) and answered by store admins (US-12).
 */
@Slf4j
@Service
public class CreditPurchaseRequestCommandServiceImpl implements CreditPurchaseRequestCommandService {

    /** An approved request becomes a single payment at the end of a 30-day month, without grace. */
    private static final SaleModalities APPROVED_REQUEST_MODALITY = SaleModalities.SINGLE_PAYMENT;
    private static final int APPROVED_REQUEST_INSTALLMENTS = 1;
    private static final int APPROVED_REQUEST_GRACE_DAYS = 0;

    private final CreditPurchaseRequestRepository purchaseRequestRepository;
    private final CreditProfileRepository creditProfileRepository;
    private final CreditSaleRepository creditSaleRepository;
    private final ExternalStoreService externalStoreService;

    public CreditPurchaseRequestCommandServiceImpl(CreditPurchaseRequestRepository purchaseRequestRepository,
                                                   CreditProfileRepository creditProfileRepository,
                                                   CreditSaleRepository creditSaleRepository,
                                                   ExternalStoreService externalStoreService) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.creditProfileRepository = creditProfileRepository;
        this.creditSaleRepository = creditSaleRepository;
        this.externalStoreService = externalStoreService;
    }

    /**
     * Checks, in order: the profile exists, its store is active, every item is sellable now in that store,
     * and the customer meets all US-08 eligibility rules for the request total. Validated here, not only
     * hidden in the frontend.
     */
    @Override
    public Result<Long, ApplicationError> handle(CreateCreditPurchaseRequestCommand command) {
        var profile = creditProfileRepository.findById(command.creditProfileId());
        if (profile.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditProfile", command.creditProfileId().toString()));
        var storeId = profile.get().getStoreId();
        if (!externalStoreService.isStoreActive(storeId))
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Store", "Store %d is deactivated and cannot receive purchase requests".formatted(storeId)));

        var details = new ArrayList<PurchaseRequestDetail>();
        for (var item : command.items()) {
            var creditPrice = externalStoreService.fetchAvailableProductCreditPrice(item.productId(), storeId);
            if (creditPrice.isEmpty())
                return Result.failure(ApplicationError.businessRuleViolation(
                        "Product", "Product %d is not available in store %d".formatted(item.productId(), storeId)));
            details.add(new PurchaseRequestDetail(item.productId(), item.quantity(), creditPrice.get()));
        }

        var total = details.stream().map(PurchaseRequestDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        var ineligibility = checkEligibility(profile.get(), total);
        if (ineligibility.isPresent()) return Result.failure(ineligibility.get());

        try {
            return Result.success(purchaseRequestRepository.save(
                    new CreditPurchaseRequest(command.creditProfileId(), details)).getId());
        } catch (Exception e) {
            log.error("Error in create-credit-purchase-request", e);
            return Result.failure(ApplicationError.unexpected("create-credit-purchase-request", "The purchase request could not be created"));
        }
    }

    /**
     * Checks, in order: the request exists and is pending, the store is still active, the customer is
     * still eligible, and the profile accepts the sale. Then registers the sale with the profile's current
     * conditions and the prices of the request, links both ways and saves everything in one transaction.
     */
    @Override
    @Transactional
    public Result<CreditPurchaseRequest, ApplicationError> handle(ApproveCreditPurchaseRequestCommand command) {
        var request = purchaseRequestRepository.findById(command.purchaseRequestId());
        if (request.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditPurchaseRequest", command.purchaseRequestId().toString()));
        if (!request.get().isPending())
            return Result.failure(ApplicationError.conflict("CreditPurchaseRequest",
                    "Request %d was already %s".formatted(request.get().getId(), request.get().getStatus())));

        var profile = creditProfileRepository.findById(request.get().getCreditProfileId()).orElseThrow();
        if (!externalStoreService.isStoreActive(profile.getStoreId()))
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Store", "Store %d is deactivated and cannot register new sales".formatted(profile.getStoreId())));
        var ineligibility = checkEligibility(profile, request.get().getTotalAmount());
        if (ineligibility.isPresent()) return Result.failure(ineligibility.get());

        var sales = creditSaleRepository.findAllByCreditProfileId(profile.getId());
        var termDays = CreditSale.termDaysOf(APPROVED_REQUEST_GRACE_DAYS, APPROVED_REQUEST_INSTALLMENTS);
        var rejection = profile.saleRejectionReason(request.get().getTotalAmount(), termDays, sales);
        if (rejection.isPresent())
            return Result.failure(ApplicationError.businessRuleViolation("CreditSale", rejection.get()));

        try {
            var sale = creditSaleRepository.save(CreditSale.register(
                    profile, LocalDate.now(CreditSale.BUSINESS_ZONE), APPROVED_REQUEST_MODALITY,
                    APPROVED_REQUEST_INSTALLMENTS, APPROVED_REQUEST_GRACE_DAYS,
                    request.get().getDetails().stream().map(PurchaseRequestDetail::toSaleDetail).toList(),
                    request.get().getId()));
            return Result.success(purchaseRequestRepository.save(request.get().approve(sale.getId())));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error in approve-credit-purchase-request", e);
            return Result.failure(ApplicationError.unexpected("approve-credit-purchase-request", "The purchase request could not be approved"));
        }
    }

    @Override
    public Result<CreditPurchaseRequest, ApplicationError> handle(RejectCreditPurchaseRequestCommand command) {
        var request = purchaseRequestRepository.findById(command.purchaseRequestId());
        if (request.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditPurchaseRequest", command.purchaseRequestId().toString()));
        if (!request.get().isPending())
            return Result.failure(ApplicationError.conflict("CreditPurchaseRequest",
                    "Request %d was already %s".formatted(request.get().getId(), request.get().getStatus())));
        try {
            return Result.success(purchaseRequestRepository.save(request.get().reject(command.reason())));
        } catch (Exception e) {
            log.error("Error in reject-credit-purchase-request", e);
            return Result.failure(ApplicationError.unexpected("reject-credit-purchase-request", "The purchase request could not be rejected"));
        }
    }

    private Optional<ApplicationError> checkEligibility(CreditProfile profile, BigDecimal amount) {
        var sales = creditSaleRepository.findAllByCreditProfileId(profile.getId());
        var assessment = PurchaseRequestEligibilityPolicy.assess(
                profile.isActive(),
                sales.stream().map(CreditSale::getStatus).toList(),
                profile.availableCredit(sales),
                amount);
        if (assessment.eligible()) return Optional.empty();
        return Optional.of(ApplicationError.businessRuleViolation("CreditPurchaseRequest",
                "Credit profile %d is not eligible for a remote purchase request: %s".formatted(profile.getId(), assessment.failures())));
    }
}
