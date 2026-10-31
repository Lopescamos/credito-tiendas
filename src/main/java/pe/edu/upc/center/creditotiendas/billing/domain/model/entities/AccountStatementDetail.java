package pe.edu.upc.center.creditotiendas.billing.domain.model.entities;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.MovementTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Row of {@code Detalle_Estado_Cuenta}: one movement in a monthly account statement.
 */
@Getter
public class AccountStatementDetail {

    @Setter private Long id;
    @Setter private MovementTypes movementType;
    @Setter private String description;
    @Setter private BigDecimal amount;
    @Setter private LocalDate date;

    public AccountStatementDetail() {
    }

    public AccountStatementDetail(MovementTypes movementType, String description, BigDecimal amount, LocalDate date) {
        this.movementType = movementType;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }
}
