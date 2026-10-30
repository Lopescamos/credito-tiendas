package pe.edu.upc.center.creditotiendas.iam.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.iam.domain.model.commands.CreateUserCommand;
import pe.edu.upc.center.creditotiendas.iam.domain.model.entities.UserStore;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.EmailAddress;
import pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Platform user (table {@code users}). Holds credentials, a single role and, for store admins,
 * the stores they are linked to.
 *
 * <p>Only the password hash is kept: hashing happens in the application layer through
 * {@code HashingService} before the aggregate is built.</p>
 */
@Getter
public class User extends AbstractDomainAggregateRoot<User> {

    @Setter private Long id;
    @Setter private EmailAddress email;
    @Setter private String firstName;
    @Setter private String lastName;
    @Setter private String passwordHash;
    @Setter private Roles role;
    @Setter private boolean active;
    private final List<UserStore> storeAssignments = new ArrayList<>();

    public User() {
        this.firstName = "";
        this.lastName = "";
        this.passwordHash = "";
        this.active = true;
    }

    public User(EmailAddress email, String firstName, String lastName, String passwordHash, Roles role) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(CreateUserCommand command, String passwordHash) {
        this(new EmailAddress(command.email()), command.firstName(), command.lastName(), passwordHash, command.role());
    }

    public User updateInformation(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public User activate() {
        this.active = true;
        return this;
    }

    public User deactivate() {
        this.active = false;
        return this;
    }

    public boolean isSystemAdmin() {
        return this.role == Roles.SYSTEM_ADMIN;
    }

    public boolean isStoreAdmin() {
        return this.role == Roles.STORE_ADMIN;
    }

    /**
     * @return whether the user has an active link to the given store
     */
    public boolean isAssignedToStore(Long storeId) {
        return findAssignment(storeId).map(UserStore::isActive).orElse(false);
    }

    /**
     * Links this store admin to a store. Reactivates a previous link instead of duplicating it.
     *
     * @throws IllegalStateException if the user is not a store admin or is already linked
     */
    public User assignToStore(Long storeId) {
        if (!isStoreAdmin())
            throw new IllegalStateException("[User] only store admins can be assigned to a store");
        if (isAssignedToStore(storeId))
            throw new IllegalStateException("[User] user is already assigned to store %d".formatted(storeId));
        findAssignment(storeId).ifPresentOrElse(
                UserStore::reactivate,
                () -> this.storeAssignments.add(new UserStore(storeId)));
        return this;
    }

    /**
     * Deactivates the link between this user and a store.
     *
     * @throws IllegalStateException if there is no active link to that store
     */
    public User unassignFromStore(Long storeId) {
        var assignment = findAssignment(storeId)
                .filter(UserStore::isActive)
                .orElseThrow(() -> new IllegalStateException(
                        "[User] user is not assigned to store %d".formatted(storeId)));
        assignment.deactivate();
        return this;
    }

    /**
     * @return ids of the stores this user is actively linked to
     */
    public List<Long> getActiveStoreIds() {
        return this.storeAssignments.stream()
                .filter(UserStore::isActive)
                .map(UserStore::getStoreId)
                .toList();
    }

    public List<UserStore> getStoreAssignments() {
        return Collections.unmodifiableList(this.storeAssignments);
    }

    /**
     * Replaces the store assignments. Used by the persistence assembler to rebuild the aggregate.
     */
    public void setStoreAssignments(List<UserStore> storeAssignments) {
        this.storeAssignments.clear();
        this.storeAssignments.addAll(storeAssignments);
    }

    private Optional<UserStore> findAssignment(Long storeId) {
        return this.storeAssignments.stream()
                .filter(assignment -> Objects.equals(assignment.getStoreId(), storeId))
                .findFirst();
    }
}
