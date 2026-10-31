package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Line of a sale or of a purchase request.
 */
public record LineItemResource(Long productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal) {
}
