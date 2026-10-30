package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.Instant;

/**
 * Table {@code user_stores} (model: Usuario_Tienda).
 *
 * <p>{@code store_id} is a plain column, not a JPA relation: stores belong to the
 * Store Management context, and contexts reference each other by id only.</p>
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "store_id"}))
@Getter @Setter @NoArgsConstructor
public class UserStorePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPersistenceEntity user;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Instant assignedAt;

    @Column(nullable = false)
    private boolean active;
}
