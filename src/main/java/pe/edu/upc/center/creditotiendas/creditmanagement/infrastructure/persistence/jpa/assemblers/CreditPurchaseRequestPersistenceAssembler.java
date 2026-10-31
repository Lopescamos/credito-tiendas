package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditPurchaseRequest;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.PurchaseRequestDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditPurchaseRequestPersistenceEntity;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.PurchaseRequestDetailPersistenceEntity;

/**
 * Maps the {@code CreditPurchaseRequest} aggregate, including its {@code PurchaseRequestDetail} children.
 */
public final class CreditPurchaseRequestPersistenceAssembler {

    private CreditPurchaseRequestPersistenceAssembler() {
    }

    public static CreditPurchaseRequest toDomainFromPersistence(CreditPurchaseRequestPersistenceEntity entity) {
        if (entity == null) return null;
        var request = new CreditPurchaseRequest();
        request.setId(entity.getId());
        request.setCreditProfileId(entity.getCreditProfileId());
        request.setRequestedAt(entity.getRequestedAt());
        request.setStatus(entity.getStatus());
        request.setRejectionReason(entity.getRejectionReason());
        request.setRespondedAt(entity.getRespondedAt());
        request.setGeneratedSaleId(entity.getGeneratedSaleId());
        request.setTotalAmount(entity.getTotalAmount());
        request.setDetails(entity.getDetails().stream()
                .map(CreditPurchaseRequestPersistenceAssembler::toDomainFromPersistence)
                .toList());
        return request;
    }

    public static CreditPurchaseRequestPersistenceEntity toPersistenceFromDomain(CreditPurchaseRequest request) {
        if (request == null) return null;
        var entity = new CreditPurchaseRequestPersistenceEntity();
        if (request.getId() != null) entity.setId(request.getId());
        entity.setCreditProfileId(request.getCreditProfileId());
        entity.setRequestedAt(request.getRequestedAt());
        entity.setStatus(request.getStatus());
        entity.setRejectionReason(request.getRejectionReason());
        entity.setRespondedAt(request.getRespondedAt());
        entity.setGeneratedSaleId(request.getGeneratedSaleId());
        entity.setTotalAmount(request.getTotalAmount());
        request.getDetails().forEach(detail -> entity.getDetails().add(toPersistenceFromDomain(detail, entity)));
        return entity;
    }

    private static PurchaseRequestDetail toDomainFromPersistence(PurchaseRequestDetailPersistenceEntity entity) {
        var detail = new PurchaseRequestDetail();
        detail.setId(entity.getId());
        detail.setProductId(entity.getProductId());
        detail.setQuantity(entity.getQuantity());
        detail.setUnitPrice(entity.getUnitPrice());
        detail.setSubtotal(entity.getSubtotal());
        return detail;
    }

    private static PurchaseRequestDetailPersistenceEntity toPersistenceFromDomain(
            PurchaseRequestDetail detail, CreditPurchaseRequestPersistenceEntity request) {
        var entity = new PurchaseRequestDetailPersistenceEntity();
        if (detail.getId() != null) entity.setId(detail.getId());
        entity.setPurchaseRequest(request);
        entity.setProductId(detail.getProductId());
        entity.setQuantity(detail.getQuantity());
        entity.setUnitPrice(detail.getUnitPrice());
        entity.setSubtotal(detail.getSubtotal());
        return entity;
    }
}
