package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditPurchaseRequestResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.LineItemResource;

public class CreditPurchaseRequestResourceFromEntityAssembler {
    public static CreditPurchaseRequestResource toResourceFromEntity(CreditPurchaseRequest request) {
        return new CreditPurchaseRequestResource(
                request.getId(),
                request.getCreditProfileId(),
                request.getRequestedAt(),
                request.getStatus().name(),
                request.getRejectionReason(),
                request.getRespondedAt(),
                request.getGeneratedSaleId(),
                request.getTotalAmount(),
                request.getDetails().stream()
                        .map(detail -> new LineItemResource(detail.getProductId(), detail.getQuantity(),
                                detail.getUnitPrice(), detail.getSubtotal()))
                        .toList());
    }
}
