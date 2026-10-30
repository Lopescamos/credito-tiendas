package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.valueobjects.Ruc;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.converters.RucPersistenceConverter;

/**
 * Table {@code stores} (model: Tienda). The model's {@code fecha_registro} is the inherited
 * {@code created_at}.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class StorePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = RucPersistenceConverter.class)
    @Column(length = 11, nullable = false, unique = true)
    private Ruc ruc;

    @Column(length = 120, nullable = false)
    private String legalName;

    @Column(length = 100, nullable = false)
    private String tradeName;

    @Column(length = 200, nullable = false)
    private String address;

    @Column(length = 15)
    private String phone;

    @Column(nullable = false)
    private boolean active;
}
