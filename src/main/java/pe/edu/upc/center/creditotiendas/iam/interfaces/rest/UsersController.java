package pe.edu.upc.center.creditotiendas.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.iam.application.commandservices.UserCommandService;
import pe.edu.upc.center.creditotiendas.iam.application.queryservices.UserQueryService;
import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.UnassignUserFromStoreCommand;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.resources.*;
import pe.edu.upc.center.creditotiendas.iam.interfaces.rest.transform.*;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * User management (US-14). Restricted to the system admin, except {@code /me}.
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User and store assignment management")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping
    @Operation(summary = "Create a store admin or client user")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserResource resource) {
        var command = CreateUserCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command)
                .flatMap(userId -> userQueryService.handle(new GetUserByIdQuery(userId))
                        .<Result<User, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(ApplicationError.notFound("User", userId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery()).stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the authenticated user")
    public ResponseEntity<UserResource> getCurrentUser(Authentication authentication) {
        return userQueryService.handle(new GetUserByEmailQuery(new EmailAddress(authentication.getName())))
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        return userQueryService.handle(new GetUserByIdQuery(userId))
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update the first and last name of a user")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserResource resource) {
        var command = UpdateUserCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                userCommandService.handle(command), UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Activate or deactivate a user")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId,
                                              @Valid @RequestBody UpdateUserStatusResource resource) {
        var command = UpdateUserStatusCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                userCommandService.handle(command), UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping("/{userId}/stores")
    @Operation(summary = "Assign a store admin to a store")
    public ResponseEntity<?> assignUserToStore(@PathVariable Long userId,
                                               @Valid @RequestBody AssignUserToStoreResource resource) {
        var command = AssignUserToStoreCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                userCommandService.handle(command), UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}/stores/{storeId}")
    @Operation(summary = "Unassign a store admin from a store")
    public ResponseEntity<?> unassignUserFromStore(@PathVariable Long userId, @PathVariable Long storeId) {
        var command = new UnassignUserFromStoreCommand(userId, storeId);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                userCommandService.handle(command), UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
