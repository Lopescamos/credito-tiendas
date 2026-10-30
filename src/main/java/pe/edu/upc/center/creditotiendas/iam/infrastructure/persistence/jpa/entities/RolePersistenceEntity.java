package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

/**
 * Table {@code roles} (model: Rol).
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class RolePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false, unique = true)
    private Roles name;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private boolean active;
}
