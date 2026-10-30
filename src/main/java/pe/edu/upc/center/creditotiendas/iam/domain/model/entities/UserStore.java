package pe.edu.upc.center.creditotiendas.iam.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Link between a store admin and a store they manage (table {@code user_stores}).
 * Owned by the {@code User} aggregate. Unassigning deactivates the link instead of deleting it,
 * so the assignment history is kept.
 *
 * <p>{@code storeId} references a store of the Store Management context by id only.</p>
 */
@Getter
public class UserStore {

    @Setter private Long id;
    @Setter private Long storeId;
    @Setter private Instant assignedAt;
    @Setter private boolean active;

    public UserStore() {
    }

    public UserStore(Long storeId) {
        this.storeId = storeId;
        this.assignedAt = Instant.now();
        this.active = true;
    }

    public void reactivate() {
        this.active = true;
        this.assignedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }
}
