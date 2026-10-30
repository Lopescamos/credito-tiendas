package pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Role catalog entry (table {@code roles}). Rows are seeded from {@link Roles} at startup;
 * there is no API to create or edit roles.
 */
@Getter
public class Role extends AbstractDomainAggregateRoot<Role> {

    @Setter private Long id;
    @Setter private Roles name;
    @Setter private String description;
    @Setter private boolean active;

    public Role() {
        this.description = "";
        this.active = true;
    }

    public Role(Roles name) {
        this.name = name;
        this.description = name.getDescription();
        this.active = true;
    }
}
