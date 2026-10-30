package pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all domain aggregate roots.
 *
 * <p>Provides domain event registration in plain Java, with no dependency on
 * Spring, Spring Data or JPA, so the domain layer stays framework-free.
 * Identity and auditing are intentionally left to the infrastructure layer
 * ({@code AuditableAbstractPersistenceEntity}).</p>
 *
 * <p>Repository adapters retrieve the registered events with {@link #domainEvents()},
 * publish them after persisting the aggregate, and then call {@link #clearDomainEvents()}.</p>
 *
 * @param <T> the concrete aggregate root type (kept so every aggregate declares
 *            itself as {@code extends AbstractDomainAggregateRoot<Product>})
 */
@NullMarked
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>> {

    private final List<Object> domainEvents = new ArrayList<>();

    /**
     * Registers a domain event to be published after this aggregate is saved.
     *
     * @param event the domain event to register
     */
    protected void registerDomainEvent(Object event) {
        Objects.requireNonNull(event, "[AbstractDomainAggregateRoot] domain event cannot be null");
        this.domainEvents.add(event);
    }

    /**
     * Returns a snapshot of the domain events registered since the last publication.
     *
     * @return an immutable copy of the registered domain events
     */
    public Collection<Object> domainEvents() {
        return List.copyOf(this.domainEvents);
    }

    /**
     * Clears all registered domain events. Called by repository adapters after publishing.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
