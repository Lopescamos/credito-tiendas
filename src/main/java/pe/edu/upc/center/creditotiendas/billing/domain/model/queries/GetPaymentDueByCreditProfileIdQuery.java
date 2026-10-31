package pe.edu.upc.center.creditotiendas.billing.domain.model.queries;

import java.time.LocalDate;
import java.util.List;

/**
 * What a customer owes today (US-05, US-10). When {@code installmentIds} is empty, every unpaid installment
 * of the profile is included; otherwise only the ones in the list.
 */
public record GetPaymentDueByCreditProfileIdQuery(Long creditProfileId, LocalDate asOf, List<Long> installmentIds) {
}
