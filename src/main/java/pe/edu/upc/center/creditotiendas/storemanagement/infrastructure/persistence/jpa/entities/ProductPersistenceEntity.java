package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.ProductTypes;

import java.math.BigDecimal;

/**
 * Table {@code products} (model: Producto_Servicio).
 *
 * <p>{@code store_id} and {@code measurement_unit_id} are plain columns: aggregates reference each
 * other by id. The foreign keys exist in the database (Flyway V2), not as JPA relations.</p>
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class ProductPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long storeId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private ProductTypes type;

    @Column(length = 100)
    private String supplier;

    @Column(length = 60)
    private String brand;

    private Long measurementUnitId;

    @Column(length = 500)
    private String imageUrl;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal cashPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal creditPrice;

    @Column(nullable = false)
    private boolean active;
}
