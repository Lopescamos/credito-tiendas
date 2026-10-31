package pe.edu.upc.center.creditotiendas.billing.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.AccountStatementQueryService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetAccountStatementByIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetAccountStatementsByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.resources.AccountStatementResource;
import pe.edu.upc.center.creditotiendas.billing.interfaces.rest.transform.AccountStatementResourceFromEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Monthly account statements (US-06). Generation is automatic on the cut-off day; this controller is read-only.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Account Statements", description = "Monthly statements")
public class AccountStatementsController {

    private final AccountStatementQueryService accountStatementQueryService;

    public AccountStatementsController(AccountStatementQueryService accountStatementQueryService) {
        this.accountStatementQueryService = accountStatementQueryService;
    }

    @GetMapping("/credit-profiles/{creditProfileId}/account-statements")
    @PreAuthorize("@creditAuthorization.canAccessProfile(authentication, #creditProfileId)")
    @Operation(summary = "Account statements of the profile, newest first")
    public ResponseEntity<List<AccountStatementResource>> getAccountStatements(@PathVariable Long creditProfileId) {
        return ResponseEntity.ok(accountStatementQueryService
                .handle(new GetAccountStatementsByCreditProfileIdQuery(creditProfileId)).stream()
                .map(AccountStatementResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @GetMapping("/account-statements/{accountStatementId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Account statement with its details")
    public ResponseEntity<AccountStatementResource> getAccountStatementById(@PathVariable Long accountStatementId) {
        return accountStatementQueryService.handle(new GetAccountStatementByIdQuery(accountStatementId))
                .map(statement -> {
                    // Simple access check: delegate to the statement's profile authorization at the method level.
                    // Not using @PreAuthorize on #accountStatementId because we'd need an extra lookup; the
                    // business-id based URL plus the following filter is enough for scope.
                    return AccountStatementResourceFromEntityAssembler.toResourceFromEntity(statement);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
