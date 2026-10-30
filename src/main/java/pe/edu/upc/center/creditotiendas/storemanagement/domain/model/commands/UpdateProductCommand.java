package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.ProductTypes;

import java.math.BigDecimal;
import java.util.Objects;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.*;

/**
 * Replaces the editable data of a catalog item. The store it belongs to cannot change.
 */
public record UpdateProductCommand(Long productId, String name, String description, ProductTypes type,
                                   String supplier, String brand, Long measurementUnitId,
                                   BigDecimal cashPrice, BigDecimal creditPrice) {
    public UpdateProductCommand {
        productId = requireId(productId, "productId");
        name = requireText(name, "name", 100);
        description = optionalText(description, "description", 300);
        if (Objects.isNull(type))
            throw new IllegalArgumentException("[type] cannot be null");
        supplier = optionalText(supplier, "supplier", 100);
        brand = optionalText(brand, "brand", 60);
        measurementUnitId = optionalId(measurementUnitId, "measurementUnitId");
        cashPrice = requirePositiveAmount(cashPrice, "cashPrice");
        creditPrice = requirePositiveAmount(creditPrice, "creditPrice");
    }
}
