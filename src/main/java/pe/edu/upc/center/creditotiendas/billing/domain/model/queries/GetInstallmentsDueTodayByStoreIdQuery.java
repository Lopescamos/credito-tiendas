package pe.edu.upc.center.creditotiendas.billing.domain.model.queries;

import java.time.LocalDate;

/**
 * Installments of a store that fall due or are overdue on {@code asOf} (US-05 dashboard).
 */
public record GetInstallmentsDueTodayByStoreIdQuery(Long storeId, LocalDate asOf) {
}
