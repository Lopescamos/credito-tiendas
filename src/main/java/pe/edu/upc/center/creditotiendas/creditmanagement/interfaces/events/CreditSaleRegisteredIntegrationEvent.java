package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.events;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.ScheduledInstallment;

import java.util.List;

/**
 * Published by Credit when a new {@code CreditSale} is persisted (US-02 confirmation). Carries the full
 * payment plan computed from the sale's rate snapshot so Billing can persist the cuotas without calling back
 * into Credit.
 *
 * <p>Spring publishes it synchronously after the sale is saved; consumers run inside the same transaction, so
 * if the Billing listener fails the sale is rolled back.</p>
 */
public record CreditSaleRegisteredIntegrationEvent(
        Long creditSaleId,
        Long creditProfileId,
        Long storeId,
        List<ScheduledInstallment> installments) {

    public CreditSaleRegisteredIntegrationEvent {
        installments = List.copyOf(installments);
    }
}
