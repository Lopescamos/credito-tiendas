package pe.edu.upc.center.creditotiendas.iam.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.AuthenticatedUser;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.UserCommandService;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.acl.ExternalStoreService;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.hashing.HashingService;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.tokens.TokenService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.*;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.iam.domain.repositories.UserRepository;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.util.function.Function;

/**
 * User management (US-14) and sign-in.
 */
@Slf4j
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final ExternalStoreService externalStoreService;

    /**
     * Compared against when the email does not exist, so an unknown email takes as long as a wrong
     * password and response times do not reveal which accounts exist.
     */
    private final String dummyPasswordHash;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService,
                                  TokenService tokenService, ExternalStoreService externalStoreService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.externalStoreService = externalStoreService;
        this.dummyPasswordHash = hashingService.encode("dummy-password-for-timing-protection");
    }

    @Override
    public Result<AuthenticatedUser, ApplicationError> handle(SignInCommand command) {
        var user = userRepository.findByEmail(new EmailAddress(command.email()));
        if (user.isEmpty()) {
            hashingService.matches(command.password(), dummyPasswordHash);
            return Result.failure(ApplicationError.unauthorized(INVALID_CREDENTIALS));
        }
        if (!hashingService.matches(command.password(), user.get().getPasswordHash()))
            return Result.failure(ApplicationError.unauthorized(INVALID_CREDENTIALS));
        if (!user.get().isActive())
            return Result.failure(ApplicationError.forbidden("User", "User account is deactivated"));

        var token = tokenService.generateToken(
                user.get().getEmail().value(), user.get().getId(), user.get().getRole().name());
        return Result.success(new AuthenticatedUser(user.get(), token));
    }

    @Override
    public Result<Long, ApplicationError> handle(CreateUserCommand command) {
        if (!command.role().isAssignableThroughUserManagement())
            return Result.failure(ApplicationError.businessRuleViolation(
                    "User", "Cannot create a user with role %s: only STORE_ADMIN or CLIENT are allowed".formatted(command.role())));

        var email = new EmailAddress(command.email());
        if (userRepository.existsByEmail(email))
            return Result.failure(ApplicationError.conflict(
                    "User", "A user with email '%s' already exists".formatted(email.value())));

        try {
            var user = userRepository.save(new User(command, hashingService.encode(command.password())));
            return Result.success(user.getId());
        } catch (Exception e) {
            log.error("Error creating user", e);
            return Result.failure(ApplicationError.unexpected("create-user", "The user could not be created"));
        }
    }

    @Override
    public Result<User, ApplicationError> handle(UpdateUserCommand command) {
        return updateUser(command.userId(), "update-user",
                user -> Result.success(user.updateInformation(command.firstName(), command.lastName())));
    }

    @Override
    public Result<User, ApplicationError> handle(UpdateUserStatusCommand command) {
        return updateUser(command.userId(), "update-user-status", user -> {
            if (user.isSystemAdmin())
                return Result.failure(ApplicationError.businessRuleViolation(
                        "User", "User %d is the system admin and cannot be activated or deactivated".formatted(user.getId())));
            return Result.success(command.active() ? user.activate() : user.deactivate());
        });
    }

    /**
     * Checks, in order: the store exists and is active (US-13: a deactivated store cannot receive
     * newly linked users), then the user is an active store admin not yet linked to that store.
     */
    @Override
    public Result<User, ApplicationError> handle(AssignUserToStoreCommand command) {
        if (!externalStoreService.existsStore(command.storeId()))
            return Result.failure(ApplicationError.notFound("Store", command.storeId().toString()));
        if (!externalStoreService.isStoreActive(command.storeId()))
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Store", "Store %d is deactivated and cannot receive newly linked users".formatted(command.storeId())));

        return updateUser(command.userId(), "assign-user-to-store", user -> {
            if (!user.isStoreAdmin())
                return Result.failure(ApplicationError.businessRuleViolation(
                        "User", "User %d has role %s: only STORE_ADMIN users can be assigned to a store".formatted(user.getId(), user.getRole())));
            if (!user.isActive())
                return Result.failure(ApplicationError.businessRuleViolation(
                        "User", "User %d is deactivated and cannot be assigned to a store".formatted(user.getId())));
            if (user.isAssignedToStore(command.storeId()))
                return Result.failure(ApplicationError.conflict(
                        "UserStore", "User %d is already assigned to store %d".formatted(user.getId(), command.storeId())));
            return Result.success(user.assignToStore(command.storeId()));
        });
    }

    @Override
    public Result<User, ApplicationError> handle(UnassignUserFromStoreCommand command) {
        return updateUser(command.userId(), "unassign-user-from-store", user -> {
            if (!user.isAssignedToStore(command.storeId()))
                return Result.failure(ApplicationError.notFound(
                        "UserStore", "user %d, store %d".formatted(user.getId(), command.storeId())));
            return Result.success(user.unassignFromStore(command.storeId()));
        });
    }

    @Override
    public void handle(SeedSystemAdminCommand command) {
        if (userRepository.existsByRole(Roles.SYSTEM_ADMIN)) return;

        var email = new EmailAddress(command.email());
        if (userRepository.existsByEmail(email)) {
            log.warn("System admin not seeded: email {} already belongs to another user", email.value());
            return;
        }
        userRepository.save(new User(email, command.firstName(), command.lastName(),
                hashingService.encode(command.password()), Roles.SYSTEM_ADMIN));
        log.info("Initial system admin {} created", email.value());
    }

    /**
     * Loads the user, applies a change that may fail with a business error, and saves it.
     */
    private Result<User, ApplicationError> updateUser(Long userId, String context,
                                                      Function<User, Result<User, ApplicationError>> change) {
        var user = userRepository.findById(userId);
        if (user.isEmpty())
            return Result.failure(ApplicationError.notFound("User", userId.toString()));

        return change.apply(user.get()).flatMap(changed -> {
            try {
                return Result.success(userRepository.save(changed));
            } catch (Exception e) {
                log.error("Error in {}", context, e);
                return Result.failure(ApplicationError.unexpected(context, "The user could not be updated"));
            }
        });
    }
}
