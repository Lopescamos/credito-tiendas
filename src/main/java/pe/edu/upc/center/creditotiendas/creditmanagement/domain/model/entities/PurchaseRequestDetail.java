package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Line of a remote credit purchase request (model: Detalle_Solicitud). The unit price is the product's
 * credit price when the customer sent the request; the sale generated on approval keeps it.
 */
@Getter
public class PurchaseRequestDetail {

    @Setter private Long id;
    @Setter private Long productId;
    @Setter private BigDecimal quantity;
    @Setter private BigDecimal unitPrice;
    @Setter private BigDecimal subtotal;

    public PurchaseRequestDetail() {
    }

    public PurchaseRequestDetail(Long productId, BigDecimal quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public SaleDetail toSaleDetail() {
        return new SaleDetail(productId, quantity, unitPrice);
    }
}
