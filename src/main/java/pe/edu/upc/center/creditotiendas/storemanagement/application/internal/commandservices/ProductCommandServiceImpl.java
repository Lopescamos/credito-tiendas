package pe.edu.upc.center.creditotiendas.storemanagement.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices.ProductCommandService;
import pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.FileStorageException;
import pe.edu.upc.center.creditotiendas.storemanagement.application.internal.outboundservices.FileStorageService;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateProductStatusCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UploadProductImageCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.MeasurementUnitRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.ProductRepository;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.repositories.StoreRepository;

import java.util.Optional;
import java.util.function.Function;

/**
 * Catalog maintenance (US-11).
 */
@Slf4j
@Service
public class ProductCommandServiceImpl implements ProductCommandService {

    private static final String IMAGE_FOLDER = "products";

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final FileStorageService fileStorageService;

    public ProductCommandServiceImpl(ProductRepository productRepository,
                                     StoreRepository storeRepository,
                                     MeasurementUnitRepository measurementUnitRepository,
                                     FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.measurementUnitRepository = measurementUnitRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Checks, in order: the store exists, the store is active, and the measurement unit
     * (when given) exists.
     */
    @Override
    public Result<Long, ApplicationError> handle(CreateProductCommand command) {
        var store = storeRepository.findById(command.storeId());
        if (store.isEmpty())
            return Result.failure(ApplicationError.notFound("Store", command.storeId().toString()));
        if (!store.get().isActive())
            return Result.failure(ApplicationError.businessRuleViolation(
                    "Store", "Store %d is deactivated and cannot add catalog items".formatted(command.storeId())));
        var unitError = validateMeasurementUnit(command.measurementUnitId());
        if (unitError.isPresent()) return Result.failure(unitError.get());

        try {
            return Result.success(productRepository.save(new Product(command)).getId());
        } catch (Exception e) {
            log.error("Error in create-product", e);
            return Result.failure(ApplicationError.unexpected("create-product", "The product could not be created"));
        }
    }

    @Override
    public Result<Product, ApplicationError> handle(UpdateProductCommand command) {
        var unitError = validateMeasurementUnit(command.measurementUnitId());
        if (unitError.isPresent()) return Result.failure(unitError.get());

        return updateProduct(command.productId(), "update-product", product -> product.updateInformation(
                command.name(), command.description(), command.type(), command.supplier(), command.brand(),
                command.measurementUnitId(), command.cashPrice(), command.creditPrice()));
    }

    @Override
    public Result<Product, ApplicationError> handle(UpdateProductStatusCommand command) {
        return updateProduct(command.productId(), "update-product-status",
                product -> command.active() ? product.activate() : product.deactivate());
    }

    /**
     * Uploads the image under a stable name per product, so a new upload replaces the previous file
     * instead of leaving orphans in storage.
     */
    @Override
    public Result<Product, ApplicationError> handle(UploadProductImageCommand command) {
        var product = productRepository.findById(command.productId());
        if (product.isEmpty())
            return Result.failure(ApplicationError.notFound("Product", command.productId().toString()));

        String imageUrl;
        try {
            imageUrl = fileStorageService.upload(command.content(), IMAGE_FOLDER, "product-" + command.productId());
        } catch (FileStorageException e) {
            log.error("Error in upload-product-image", e);
            return Result.failure(ApplicationError.unexpected("upload-product-image", e.getMessage()));
        }
        return updateProduct(command.productId(), "upload-product-image", p -> p.updateImage(imageUrl));
    }

    private Optional<ApplicationError> validateMeasurementUnit(Long measurementUnitId) {
        if (measurementUnitId != null && !measurementUnitRepository.existsById(measurementUnitId))
            return Optional.of(ApplicationError.notFound("MeasurementUnit", measurementUnitId.toString()));
        return Optional.empty();
    }

    private Result<Product, ApplicationError> updateProduct(Long productId, String context,
                                                            Function<Product, Product> change) {
        var product = productRepository.findById(productId);
        if (product.isEmpty())
            return Result.failure(ApplicationError.notFound("Product", productId.toString()));
        try {
            return Result.success(productRepository.save(change.apply(product.get())));
        } catch (Exception e) {
            log.error("Error in {}", context, e);
            return Result.failure(ApplicationError.unexpected(context, "The product could not be updated"));
        }
    }
}
