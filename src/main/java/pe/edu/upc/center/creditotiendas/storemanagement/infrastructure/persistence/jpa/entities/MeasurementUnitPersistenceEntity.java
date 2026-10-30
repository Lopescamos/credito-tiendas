package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Table {@code measurement_units} (model: Unidad_Medida).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class MeasurementUnitPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @Column(length = 10, nullable = false)
    private String abbreviation;
}
