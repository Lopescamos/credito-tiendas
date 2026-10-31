package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.CreditSaleStatuses;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.SaleModalities;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables.InterestRatePersistenceEmbeddable;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.embeddables.LateInterestRatePersistenceEmbeddable;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Table {@code credit_sales} (model: Venta_Credito), with the 5 rate snapshot columns.
 * Details are loaded eagerly: they are always needed to rebuild the aggregate.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class CreditSalePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long creditProfileId;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SaleModalities modality;

    @Column(nullable = false)
    private int termDays;

    @Column(nullable = false)
    private int graceDays;

    @Column(nullable = false)
    private int installmentCount;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal fixedInstallment;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal financedCapital;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "percentage", column = @Column(name = "interest_rate_snapshot", precision = 10, scale = 7, nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "rate_type_snapshot", length = 20, nullable = false)),
            @AttributeOverride(name = "capitalizationPeriod", column = @Column(name = "capitalization_period_snapshot", length = 20))
    })
    private InterestRatePersistenceEmbeddable interestRateSnapshot;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "percentage", column = @Column(name = "late_interest_rate_snapshot", precision = 10, scale = 7, nullable = false)),
            @AttributeOverride(name = "type", column = @Column(name = "late_interest_rate_type_snapshot", length = 20, nullable = false))
    })
    private LateInterestRatePersistenceEmbeddable lateInterestRateSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CreditSaleStatuses status;

    private Long originPurchaseRequestId;

    @OneToMany(mappedBy = "creditSale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleDetailPersistenceEntity> details = new ArrayList<>();
}
