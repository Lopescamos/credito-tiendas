package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CapitalizationPeriods;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.RateTypes;

import java.math.BigDecimal;

/**
 * Columns of the InterestRate value object. Each owner renames them with {@code @AttributeOverrides}.
 */
@Embeddable
@Getter @NoArgsConstructor @AllArgsConstructor
public class InterestRatePersistenceEmbeddable {

    @Column(precision = 10, scale = 7)
    private BigDecimal percentage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RateTypes type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CapitalizationPeriods capitalizationPeriod;
}
