package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.StoreCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.StoreQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Store;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllStoresQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetStoreByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.*;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/stores", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Stores", description = "Store (business) management")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class StoresController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;

    public StoresController(StoreCommandService storeCommandService, StoreQueryService storeQueryService) {
        this.storeCommandService = storeCommandService;
        this.storeQueryService = storeQueryService;
    }

    @PostMapping
    @Operation(summary = "Register a store")
    public ResponseEntity<?> createStore(@Valid @RequestBody CreateStoreResource resource) {
        var command = CreateStoreCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = storeCommandService.handle(command)
                .flatMap(storeId -> storeQueryService.handle(new GetStoreByIdQuery(storeId))
                        .<Result<Store, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(ApplicationError.notFound("Store", storeId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, StoreResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all stores")
    public ResponseEntity<List<StoreResource>> getAllStores() {
        var stores = storeQueryService.handle(new GetAllStoresQuery()).stream()
                .map(StoreResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get store by ID (any authenticated user: store data is shown to its customers)")
    public ResponseEntity<StoreResource> getStoreById(@PathVariable Long storeId) {
        return storeQueryService.handle(new GetStoreByIdQuery(storeId))
                .map(StoreResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{storeId}")
    @Operation(summary = "Update store information (the RUC is not editable)")
    public ResponseEntity<?> updateStore(@PathVariable Long storeId, @Valid @RequestBody UpdateStoreResource resource) {
        var command = UpdateStoreCommandFromResourceAssembler.toCommandFromResource(storeId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                storeCommandService.handle(command), StoreResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PatchMapping("/{storeId}/status")
    @Operation(summary = "Activate or deactivate a store")
    public ResponseEntity<?> updateStoreStatus(@PathVariable Long storeId,
                                               @Valid @RequestBody UpdateStoreStatusResource resource) {
        var command = UpdateStoreStatusCommandFromResourceAssembler.toCommandFromResource(storeId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                storeCommandService.handle(command), StoreResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
