package pe.edu.upc.center.creditotiendas.iam.domain.repositories;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.Role;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;

import java.util.List;

/**
 * Persistence port of the {@link Role} aggregate.
 */
public interface RoleRepository {
    List<Role> findAll();
    boolean existsByName(Roles name);
    Role save(Role role);
}
