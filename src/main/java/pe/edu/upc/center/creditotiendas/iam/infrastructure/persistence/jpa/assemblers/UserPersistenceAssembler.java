package pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates.User;
import pe.edu.upc.center.creditotiendas.iam.domain.model.entities.UserStore;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import pe.edu.upc.center.creditotiendas.iam.infrastructure.persistence.jpa.entities.UserStorePersistenceEntity;

/**
 * Maps the {@code User} aggregate, including its {@code UserStore} children.
 *
 * <p>{@link #toPersistenceFromDomain} receives the role entity from the adapter, because the
 * domain only knows the {@code Roles} enum while the table keeps a foreign key to {@code roles}.</p>
 */
public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {
    }

    public static User toDomainFromPersistence(UserPersistenceEntity entity) {
        if (entity == null) return null;
        var user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setPasswordHash(entity.getPasswordHash());
        user.setRole(entity.getRole().getName());
        user.setActive(entity.isActive());
        user.setStoreAssignments(entity.getStoreAssignments().stream()
                .map(UserPersistenceAssembler::toDomainFromPersistence)
                .toList());
        return user;
    }

    public static UserPersistenceEntity toPersistenceFromDomain(User user, RolePersistenceEntity role) {
        if (user == null) return null;
        var entity = new UserPersistenceEntity();
        if (user.getId() != null) entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRole(role);
        entity.setActive(user.isActive());
        user.getStoreAssignments().forEach(assignment ->
                entity.getStoreAssignments().add(toPersistenceFromDomain(assignment, entity)));
        return entity;
    }

    private static UserStore toDomainFromPersistence(UserStorePersistenceEntity entity) {
        var assignment = new UserStore();
        assignment.setId(entity.getId());
        assignment.setStoreId(entity.getStoreId());
        assignment.setAssignedAt(entity.getAssignedAt());
        assignment.setActive(entity.isActive());
        return assignment;
    }

    private static UserStorePersistenceEntity toPersistenceFromDomain(UserStore assignment, UserPersistenceEntity user) {
        var entity = new UserStorePersistenceEntity();
        if (assignment.getId() != null) entity.setId(assignment.getId());
        entity.setUser(user);
        entity.setStoreId(assignment.getStoreId());
        entity.setAssignedAt(assignment.getAssignedAt());
        entity.setActive(assignment.isActive());
        return entity;
    }
}
