package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Table {@code account_statements} (model: Estado_Cuenta_Mensual) with its detail rows. {@code period} is
 * stored as {@code VARCHAR(7)} with the ISO year-month ({@code 2026-01}) so it is portable and sortable.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class AccountStatementPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long creditProfileId;

    @Column(length = 7, nullable = false)
    private String period;

    @Column(nullable = false)
    private LocalDate cutoffDate;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal previousBalance;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal periodPurchases;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal periodPayments;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal periodInterest;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal periodLateFees;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal finalBalance;

    @OneToMany(mappedBy = "accountStatement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AccountStatementDetailPersistenceEntity> details = new ArrayList<>();
}
