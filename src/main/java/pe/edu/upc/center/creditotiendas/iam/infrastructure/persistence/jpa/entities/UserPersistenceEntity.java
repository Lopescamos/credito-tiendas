package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.converters.EmailAddressPersistenceConverter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Table {@code users} (model: Usuario). The model's {@code fecha_creacion} is the inherited
 * {@code created_at}.
 *
 * <p>Role and store assignments are loaded eagerly: both are small, always needed to rebuild
 * the aggregate, and the assembler runs outside a transaction ({@code open-in-view=false}).</p>
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class UserPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = EmailAddressPersistenceConverter.class)
    @Column(length = EmailAddress.MAX_LENGTH, nullable = false, unique = true)
    private EmailAddress email;

    @Column(length = 60, nullable = false)
    private String firstName;

    @Column(length = 60, nullable = false)
    private String lastName;

    @Column(length = 256, nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RolePersistenceEntity role;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<UserStorePersistenceEntity> storeAssignments = new ArrayList<>();
}
