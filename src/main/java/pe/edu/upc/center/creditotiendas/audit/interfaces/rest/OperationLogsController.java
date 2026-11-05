package pe.edu.upc.center.creditotiendas.audit.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.center.creditotiendas.audit.application.queryservices.OperationLogQueryService;
import pe.edu.upc.center.creditotiendas.audit.domain.model.queries.GetOperationLogsQuery;
import pe.edu.upc.center.creditotiendas.audit.domain.model.valueobjects.OperationActions;
import pe.edu.upc.center.creditotiendas.audit.interfaces.rest.resources.OperationLogResource;
import pe.edu.upc.center.creditotiendas.audit.interfaces.rest.transform.OperationLogResourceFromEntityAssembler;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Read-only console of the operation log for the system admin (US-16). No POST/PUT/PATCH/DELETE
 * on purpose: the only writer is {@code OperationAuditAspect}, and the acceptance criteria say
 * entries must never be editable or deletable from the API.
 */
@RestController
@RequestMapping(value = "/api/v1/operation-logs", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Operation Logs", description = "Audit trail of system operations (US-16)")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class OperationLogsController {

    private final OperationLogQueryService operationLogQueryService;

    public OperationLogsController(OperationLogQueryService operationLogQueryService) {
        this.operationLogQueryService = operationLogQueryService;
    }

    @GetMapping
    @Operation(summary = "List operation log entries, filterable by user, action, entity and date range")
    public ResponseEntity<List<OperationLogResource>> getOperationLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OperationActions action,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        var logs = operationLogQueryService.handle(new GetOperationLogsQuery(userId, action, entity, from, to));
        return ResponseEntity.ok(logs.stream()
                .map(OperationLogResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
