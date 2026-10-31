package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables.InterestRatePersistenceEmbeddable;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables.LateInterestRatePersistenceEmbeddable;

/**
 * Maps the rate value objects to their embeddables, shared by profiles (current conditions) and sales (snapshot).
 */
public final class InterestRatePersistenceAssembler {

    private InterestRatePersistenceAssembler() {
    }

    public static InterestRate toDomainFromPersistence(InterestRatePersistenceEmbeddable embeddable) {
        if (embeddable == null) return null;
        return new InterestRate(embeddable.getPercentage(), embeddable.getType(), embeddable.getCapitalizationPeriod());
    }

    public static InterestRatePersistenceEmbeddable toPersistenceFromDomain(InterestRate rate) {
        if (rate == null) return null;
        return new InterestRatePersistenceEmbeddable(rate.percentage(), rate.type(), rate.capitalizationPeriod());
    }

    public static LateInterestRate toDomainFromPersistence(LateInterestRatePersistenceEmbeddable embeddable) {
        if (embeddable == null) return null;
        return new LateInterestRate(embeddable.getPercentage(), embeddable.getType());
    }

    public static LateInterestRatePersistenceEmbeddable toPersistenceFromDomain(LateInterestRate rate) {
        if (rate == null) return null;
        return new LateInterestRatePersistenceEmbeddable(rate.percentage(), rate.type());
    }
}
