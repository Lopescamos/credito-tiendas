package pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Snapshot of the late fee of an overdue installment at a point in time (model: Cargo_Mora).
 *
 * <p>Billing stores one row per time it (re)computes the late fee of an installment, so there is a trail of
 * {@code daysLate} and {@code amount} values; the newest row is the current late fee. The amount comes from
 * {@code LateFeeCalculator}; this aggregate only persists the result.</p>
 */
@Getter
public class LateFee extends AbstractDomainAggregateRoot<LateFee> {

    @Setter private Long id;
    @Setter private Long installmentId;
    @Setter private int daysLate;
    @Setter private BigDecimal amount;
    @Setter private Instant calculatedAt;

    public LateFee() {
    }

    public LateFee(Long installmentId, int daysLate, BigDecimal amount) {
        this.installmentId = installmentId;
        this.daysLate = daysLate;
        this.amount = amount;
        this.calculatedAt = Instant.now();
    }
}
