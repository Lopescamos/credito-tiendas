package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.PurchaseRequestDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PurchaseRequestStatuses;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A customer's remote request to buy catalog items on credit (model: Solicitud_Compra_Credito +
 * Detalle_Solicitud, US-08). It stays PENDING until the store admin approves it, which generates a
 * credit sale, or rejects it with a mandatory reason (US-12). Only a pending request can be answered.
 */
@Getter
public class CreditPurchaseRequest extends AbstractDomainAggregateRoot<CreditPurchaseRequest> {

    @Setter private Long id;
    @Setter private Long creditProfileId;
    @Setter private Instant requestedAt;
    @Setter private PurchaseRequestStatuses status;
    @Setter private String rejectionReason;
    @Setter private Instant respondedAt;
    @Setter private Long generatedSaleId;
    @Setter private BigDecimal totalAmount;
    private final List<PurchaseRequestDetail> details = new ArrayList<>();

    public CreditPurchaseRequest() {
    }

    public CreditPurchaseRequest(Long creditProfileId, List<PurchaseRequestDetail> details) {
        if (details.isEmpty())
            throw new IllegalArgumentException("[CreditPurchaseRequest] a request needs at least one detail");
        this.creditProfileId = creditProfileId;
        this.requestedAt = Instant.now();
        this.status = PurchaseRequestStatuses.PENDING;
        this.details.addAll(details);
        this.totalAmount = details.stream().map(PurchaseRequestDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isPending() {
        return status == PurchaseRequestStatuses.PENDING;
    }

    /**
     * @throws IllegalStateException if the request was already answered
     */
    public CreditPurchaseRequest approve(Long generatedSaleId) {
        requirePending();
        this.status = PurchaseRequestStatuses.APPROVED;
        this.generatedSaleId = generatedSaleId;
        this.respondedAt = Instant.now();
        return this;
    }

    /**
     * @throws IllegalStateException if the request was already answered
     */
    public CreditPurchaseRequest reject(String reason) {
        requirePending();
        this.status = PurchaseRequestStatuses.REJECTED;
        this.rejectionReason = reason;
        this.respondedAt = Instant.now();
        return this;
    }

    public List<PurchaseRequestDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }

    /**
     * Replaces the details. Used by the persistence assembler to rebuild the aggregate.
     */
    public void setDetails(List<PurchaseRequestDetail> details) {
        this.details.clear();
        this.details.addAll(details);
    }

    private void requirePending() {
        if (!isPending())
            throw new IllegalStateException("[CreditPurchaseRequest] request %d was already %s".formatted(id, status));
    }
}
