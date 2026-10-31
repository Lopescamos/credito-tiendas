package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses;
import pe.edu.upc.center.creditotiendas.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Table {@code installments} (model: Cuota_Cronograma). {@code credit_sale_id} is a plain column pointing to
 * credit_sales (another context); the FK lives in Flyway V4.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class InstallmentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private Long creditSaleId;

    @Column(nullable = false)
    private Long creditProfileId;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal capital;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal interest;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal fixedInstallment;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal remainingBalance;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InstallmentStatuses status;

    private LocalDate paymentDate;
}
