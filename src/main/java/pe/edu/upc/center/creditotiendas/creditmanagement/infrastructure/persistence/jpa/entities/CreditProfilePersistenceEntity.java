package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables.InterestRatePersistenceEmbeddable;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables.LateInterestRatePersistenceEmbeddable;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;

/**
 * Table {@code credit_profiles} (model: Perfil_Credito_Cliente). The registration date of the model is
 * the inherited {@code created_at}. user_id, store_id and currency_id point to other contexts by id;
 * their foreign keys live in the Flyway migration.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "store_id"}))
@Getter @Setter @NoArgsConstructor
public class CreditProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "percentage", column = @Column(name = "interest_rate", precision = 10, scale = 7, nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "rate_type", length = 20, nullable = false)),
            @AttributeOverride(name = "capitalizationPeriod", column = @Column(name = "capitalization_period", length = 20))
    })
    private InterestRatePersistenceEmbeddable interestRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "percentage", column = @Column(name = "late_interest_rate", precision = 10, scale = 7, nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "late_interest_rate_type", length = 20, nullable = false))
    })
    private LateInterestRatePersistenceEmbeddable lateInterestRate;

    @Column(nullable = false)
    private Long currencyId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal creditLimit;

    @Column(nullable = false)
    private int maxTermDays;

    @Column(nullable = false)
    private int cutoffDay;

    @Column(nullable = false)
    private int paymentDay;

    @Column(nullable = false)
    private boolean active;
}
