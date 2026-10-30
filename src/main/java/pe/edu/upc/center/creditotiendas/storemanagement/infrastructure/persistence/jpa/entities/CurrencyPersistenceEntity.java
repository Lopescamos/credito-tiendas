package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Table {@code currencies} (model: Moneda).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class CurrencyPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(length = 3, nullable = false, unique = true)
    private String code;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 5, nullable = false)
    private String symbol;
}
