package pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.commandservices.CreditProfileCommandService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalIamService;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.internal.outboundservices.acl.ExternalStoreService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.CreateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.UpdateCreditProfileCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.commands.UpdateCreditProfileStatusCommand;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.repositories.CreditProfileRepository;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.util.function.Function;

/**
 * Customer credit profiles (US-04).
 */
@Slf4j
@Service
public class CreditProfileCommandServiceImpl implements CreditProfileCommandService {

    private final CreditProfileRepository creditProfileRepository;
    private final ExternalIamService externalIamService;
    private final ExternalStoreService externalStoreService;

    public CreditProfileCommandServiceImpl(CreditProfileRepository creditProfileRepository,
                                           ExternalIamService externalIamService,
                                           ExternalStoreService externalStoreService) {
        this.creditProfileRepository = creditProfileRepository;
        this.externalIamService = externalIamService;
        this.externalStoreService = externalStoreService;
    }

    /**
     * Checks, in order: the store exists and is active, a user with that email exists and is a CLIENT,
     * the currency exists, and the customer has no profile in this store yet.
     */
    @Override
    public Result<Long, ApplicationError> handle(CreateCreditProfileCommand command) {
        if (!externalStoreService.existsStore(command.storeId()))
            return Result.failure(ApplicationError.notFound("Store", command.storeId().toString()));
        if (!externalStoreService.isStoreActive(command.storeId()))
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Store", "Store %d is deactivated and cannot open credit profiles".formatted(command.storeId())));

        var userId = externalIamService.fetchUserIdByEmail(command.clientEmail());
        if (userId.isEmpty())
            return Result.failure(ApplicationError.notFound("User", command.clientEmail()));
        if (!externalIamService.isClient(userId.get()))
            return Result.failure(ApplicationError.businessRuleViolation(
                    "CreditProfile", "User '%s' is not a CLIENT and cannot have a credit profile".formatted(command.clientEmail())));

        if (!externalStoreService.existsCurrency(command.currencyId()))
            return Result.failure(ApplicationError.notFound("Currency", command.currencyId().toString()));
        if (creditProfileRepository.existsByUserIdAndStoreId(userId.get(), command.storeId()))
            return Result.failure(ApplicationError.conflict("CreditProfile",
                    "User '%s' already has a credit profile in store %d".formatted(command.clientEmail(), command.storeId())));

        try {
            return Result.success(creditProfileRepository.save(new CreditProfile(command, userId.get())).getId());
        } catch (Exception e) {
            log.error("Error in create-credit-profile", e);
            return Result.failure(ApplicationError.unexpected("create-credit-profile", "The credit profile could not be created"));
        }
    }

    @Override
    public Result<CreditProfile, ApplicationError> handle(UpdateCreditProfileCommand command) {
        if (!externalStoreService.existsCurrency(command.currencyId()))
            return Result.failure(ApplicationError.notFound("Currency", command.currencyId().toString()));
        return updateProfile(command.creditProfileId(), "update-credit-profile", profile -> profile.updateConditions(
                command.interestRate(), command.lateInterestRate(), command.currencyId(), command.creditLimit(),
                command.maxTermDays(), command.cutoffDay(), command.paymentDay()));
    }

    @Override
    public Result<CreditProfile, ApplicationError> handle(UpdateCreditProfileStatusCommand command) {
        return updateProfile(command.creditProfileId(), "update-credit-profile-status",
                profile -> command.active() ? profile.activate() : profile.deactivate());
    }

    private Result<CreditProfile, ApplicationError> updateProfile(Long profileId, String context,
                                                                  Function<CreditProfile, CreditProfile> change) {
        var profile = creditProfileRepository.findById(profileId);
        if (profile.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditProfile", profileId.toString()));
        try {
            return Result.success(creditProfileRepository.save(change.apply(profile.get())));
        } catch (Exception e) {
            log.error("Error in {}", context, e);
            return Result.failure(ApplicationError.unexpected(context, "The credit profile could not be updated"));
        }
    }
}
