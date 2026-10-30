package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ResponseEntityAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.ProductCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.queryservices.ProductQueryService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UploadProductImageCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.queries.GetProductByIdQuery;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.ProductResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.UpdateProductResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources.UpdateProductStatusResource;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.ProductResourceFromEntityAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.UpdateProductCommandFromResourceAssembler;
import pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.transform.UpdateProductStatusCommandFromResourceAssembler;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Operations on a single catalog item. Only store admins of the store that owns the item.
 */
@RestController
@RequestMapping(value = "/api/v1/products", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "Catalog item maintenance")
@PreAuthorize("@storeAuthorization.canManageProduct(authentication, #productId)")
public class ProductsController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public ProductsController(ProductCommandService productCommandService, ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResource> getProductById(@PathVariable Long productId) {
        return productQueryService.handle(new GetProductByIdQuery(productId))
                .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update a catalog item")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @Valid @RequestBody UpdateProductResource resource) {
        var command = UpdateProductCommandFromResourceAssembler.toCommandFromResource(productId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                productCommandService.handle(command), ProductResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PatchMapping("/{productId}/status")
    @Operation(summary = "Activate or deactivate a catalog item (replaces deleting it)")
    public ResponseEntity<?> updateProductStatus(@PathVariable Long productId,
                                                 @Valid @RequestBody UpdateProductStatusResource resource) {
        var command = UpdateProductStatusCommandFromResourceAssembler.toCommandFromResource(productId, resource);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                productCommandService.handle(command), ProductResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace the product image (JPEG, PNG or WebP, up to 5 MB)")
    public ResponseEntity<?> uploadProductImage(@PathVariable Long productId,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        var command = new UploadProductImageCommand(productId, file.getBytes(), file.getContentType());
        return ResponseEntityAssembler.toResponseEntityFromResult(
                productCommandService.handle(command), ProductResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
