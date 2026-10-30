package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.Role;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;

public final class RolePersistenceAssembler {

    private RolePersistenceAssembler() {
    }

    public static Role toDomainFromPersistence(RolePersistenceEntity entity) {
        if (entity == null) return null;
        var role = new Role();
        role.setId(entity.getId());
        role.setName(entity.getName());
        role.setDescription(entity.getDescription());
        role.setActive(entity.isActive());
        return role;
    }

    public static RolePersistenceEntity toPersistenceFromDomain(Role role) {
        if (role == null) return null;
        var entity = new RolePersistenceEntity();
        if (role.getId() != null) entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setActive(role.isActive());
        return entity;
    }
}
