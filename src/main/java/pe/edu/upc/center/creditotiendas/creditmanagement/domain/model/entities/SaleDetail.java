package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Line of a credit sale (model: Detalle_Venta). The unit price is the product's credit price at the
 * moment of the sale and never changes afterwards.
 */
@Getter
public class SaleDetail {

    @Setter private Long id;
    @Setter private Long productId;
    @Setter private BigDecimal quantity;
    @Setter private BigDecimal unitPrice;
    @Setter private BigDecimal subtotal;

    public SaleDetail() {
    }

    public SaleDetail(Long productId, BigDecimal quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }
}
