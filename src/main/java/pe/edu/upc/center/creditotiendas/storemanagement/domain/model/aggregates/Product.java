package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.ProductTypes;

import java.math.BigDecimal;

/**
 * A product or service in a store's catalog (US-11, table {@code products}, model: Producto_Servicio).
 * There is no stock. A deactivated product is hidden from the customer catalog (US-08) but stays
 * referenced by past sales, so products are never deleted.
 */
@Getter
public class Product extends AbstractDomainAggregateRoot<Product> {

    @Setter private Long id;
    @Setter private Long storeId;
    @Setter private String name;
    @Setter private String description;
    @Setter private ProductTypes type;
    @Setter private String supplier;
    @Setter private String brand;
    @Setter private Long measurementUnitId;
    @Setter private String imageUrl;
    @Setter private BigDecimal cashPrice;
    @Setter private BigDecimal creditPrice;
    @Setter private boolean active;

    public Product() {
        this.name = "";
        this.active = true;
    }

    public Product(CreateProductCommand command) {
        this();
        this.storeId = command.storeId();
        this.name = command.name();
        this.description = command.description();
        this.type = command.type();
        this.supplier = command.supplier();
        this.brand = command.brand();
        this.measurementUnitId = command.measurementUnitId();
        this.cashPrice = command.cashPrice();
        this.creditPrice = command.creditPrice();
    }

    public Product updateInformation(String name, String description, ProductTypes type, String supplier,
                                     String brand, Long measurementUnitId,
                                     BigDecimal cashPrice, BigDecimal creditPrice) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.supplier = supplier;
        this.brand = brand;
        this.measurementUnitId = measurementUnitId;
        this.cashPrice = cashPrice;
        this.creditPrice = creditPrice;
        return this;
    }

    public Product updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Product activate() {
        this.active = true;
        return this;
    }

    public Product deactivate() {
        this.active = false;
        return this;
    }
}
