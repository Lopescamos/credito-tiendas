package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.ProductCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.ProductQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetActiveProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetAllProductsByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.CreateProductResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.ProductResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.CreateProductCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.ProductResourceFromEntityAssembler;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Catalog of one store (US-11 for store admins, US-08 catalog for clients).
 */
@RestController
@RequestMapping(value = "/api/v1/stores/{storeId}/products", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Store Products", description = "Catalog of a store")
public class StoreProductsController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public StoreProductsController(ProductCommandService productCommandService, ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @PostMapping
    @PreAuthorize("@storeAuthorization.canManageStore(authentication, #storeId)")
    @Operation(summary = "Add a product or service to the store catalog (store admin of the store)")
    public ResponseEntity<?> createProduct(@PathVariable Long storeId, @Valid @RequestBody CreateProductResource resource) {
        var command = CreateProductCommandFromResourceAssembler.toCommandFromResource(storeId, resource);
        var result = productCommandService.handle(command)
                .flatMap(productId -> productQueryService.handle(new GetProductByIdQuery(productId))
                        .<Result<Product, ApplicationError>>map(Result::success)
                        .orElseGet(() -> Result.failure(ApplicationError.notFound("Product", productId.toString()))));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ProductResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("@storeAuthorization.canViewCatalog(authentication, #storeId)")
    @Operation(summary = "Get the store catalog: all items for its store admins, only active items for clients")
    public ResponseEntity<List<ProductResource>> getStoreProducts(@PathVariable Long storeId, Authentication authentication) {
        boolean clientView = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENT".equals(authority.getAuthority()));
        var products = clientView
                ? productQueryService.handle(new GetActiveProductsByStoreIdQuery(storeId))
                : productQueryService.handle(new GetAllProductsByStoreIdQuery(storeId));
        return ResponseEntity.ok(products.stream()
                .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
