package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.ProductTypes;

import java.math.BigDecimal;
import java.util.Objects;

import static pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.StoreManagementCommandRules.*;

/**
 * Adds a product or service to a store catalog (US-11). Prices are normalized to 2 decimals.
 *
 * @param measurementUnitId optional; services usually have no unit
 */
public record CreateProductCommand(Long storeId, String name, String description, ProductTypes type,
                                   String supplier, String brand, Long measurementUnitId,
                                   BigDecimal cashPrice, BigDecimal creditPrice) {
    public CreateProductCommand {
        storeId = requireId(storeId, "storeId");
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
