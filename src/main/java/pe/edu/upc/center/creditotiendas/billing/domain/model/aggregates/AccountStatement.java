package pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.billing.domain.model.entities.AccountStatementDetail;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Monthly account statement of a credit profile (model: Estado_Cuenta_Mensual + Detalle_Estado_Cuenta, US-06).
 *
 * <p>Snapshot: once generated on the cutoff date it is not recomputed, even if conditions change later.</p>
 */
@Getter
public class AccountStatement extends AbstractDomainAggregateRoot<AccountStatement> {

    @Setter private Long id;
    @Setter private Long creditProfileId;
    @Setter private YearMonth period;
    @Setter private LocalDate cutoffDate;
    @Setter private BigDecimal previousBalance;
    @Setter private BigDecimal periodPurchases;
    @Setter private BigDecimal periodPayments;
    @Setter private BigDecimal periodInterest;
    @Setter private BigDecimal periodLateFees;
    @Setter private BigDecimal finalBalance;
    private final List<AccountStatementDetail> details = new ArrayList<>();

    public AccountStatement() {
    }

    public AccountStatement(Long creditProfileId, YearMonth period, LocalDate cutoffDate, BigDecimal previousBalance,
                            BigDecimal periodPurchases, BigDecimal periodPayments, BigDecimal periodInterest,
                            BigDecimal periodLateFees, BigDecimal finalBalance, List<AccountStatementDetail> details) {
        this.creditProfileId = creditProfileId;
        this.period = period;
        this.cutoffDate = cutoffDate;
        this.previousBalance = previousBalance;
        this.periodPurchases = periodPurchases;
        this.periodPayments = periodPayments;
        this.periodInterest = periodInterest;
        this.periodLateFees = periodLateFees;
        this.finalBalance = finalBalance;
        this.details.addAll(details);
    }

    public List<AccountStatementDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    /**
     * Replaces the details. Used by the persistence assembler to rebuild the aggregate.
     */
    public void setDetails(List<AccountStatementDetail> details) {
        this.details.clear();
        this.details.addAll(details);
    }
}
