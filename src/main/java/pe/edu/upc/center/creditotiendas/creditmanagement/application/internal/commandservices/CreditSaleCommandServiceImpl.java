package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditSaleCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalStoreService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.RegisterCreditSaleCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.SimulateCreditSaleCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.SaleDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RequestedItem;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.SaleModalities;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditSaleRepository;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Credit sales registered by the store admin (US-02).
 */
@Slf4j
@Service
public class CreditSaleCommandServiceImpl implements CreditSaleCommandService {

    private final CreditProfileRepository creditProfileRepository;
    private final CreditSaleRepository creditSaleRepository;
    private final ExternalStoreService externalStoreService;

    public CreditSaleCommandServiceImpl(CreditProfileRepository creditProfileRepository,
                                        CreditSaleRepository creditSaleRepository,
                                        ExternalStoreService externalStoreService) {
        this.creditProfileRepository = creditProfileRepository;
        this.creditSaleRepository = creditSaleRepository;
        this.externalStoreService = externalStoreService;
    }

    @Override
    public Result<Long, ApplicationError> handle(RegisterCreditSaleCommand command) {
        return prepareSale(command.creditProfileId(), command.modality(), command.installmentCount(),
                command.graceDays(), command.items())
                .flatMap(sale -> {
                    try {
                        return Result.success(creditSaleRepository.save(sale).getId());
                    } catch (Exception e) {
                        log.error("Error in register-credit-sale", e);
                        return Result.failure(ApplicationError.unexpected("register-credit-sale", "The credit sale could not be registered"));
                    }
                });
    }

    @Override
    public Result<CreditSale, ApplicationError> handle(SimulateCreditSaleCommand command) {
        return prepareSale(command.creditProfileId(), command.modality(), command.installmentCount(),
                command.graceDays(), command.items());
    }

    /**
     * Checks, in order: the profile exists, its store is active (US-13), every item is sellable now in that
     * store (its credit price is taken from the catalog), and the profile rules accept the total and term
     * (active profile, available credit, maximum term). Then builds the sale with today's date in Lima.
     */
    private Result<CreditSale, ApplicationError> prepareSale(Long creditProfileId, SaleModalities modality,
                                                             int installmentCount, int graceDays,
                                                             List<RequestedItem> items) {
        var profile = creditProfileRepository.findById(creditProfileId);
        if (profile.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditProfile", creditProfileId.toString()));
        var storeId = profile.get().getStoreId();
        if (!externalStoreService.isStoreActive(storeId))
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Store", "Store %d is deactivated and cannot register new sales".formatted(storeId)));

        var details = new ArrayList<SaleDetail>();
        for (var item : items) {
            var creditPrice = externalStoreService.fetchAvailableProductCreditPrice(item.productId(), storeId);
            if (creditPrice.isEmpty())
                return Result.failure(ApplicationError.businessRuleViolation(
                        "Product", "Product %d is not available in store %d".formatted(item.productId(), storeId)));
            details.add(new SaleDetail(item.productId(), item.quantity(), creditPrice.get()));
        }

        var total = details.stream().map(SaleDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        var sales = creditSaleRepository.findAllByCreditProfileId(creditProfileId);
        var rejection = profile.get().saleRejectionReason(total, CreditSale.termDaysOf(graceDays, installmentCount), sales);
        if (rejection.isPresent())
            return Result.failure(ApplicationError.businessRuleViolation("CreditSale", rejection.get()));

        return Result.success(CreditSale.register(profile.get(), LocalDate.now(CreditSale.BUSINESS_ZONE),
                modality, installmentCount, graceDays, details, null));
    }
}
