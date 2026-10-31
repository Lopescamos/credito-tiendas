package pe.edu.upc.center.creditotiendas.billing.domain.repositories;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link Payment} aggregate, including its allocations.
 */
public interface PaymentRepository {
    Optional<Payment> findById(Long id);
    List<Payment> findAllByCreditProfileId(Long creditProfileId);
    BigDecimal sumAmountByCreditProfileIdInRange(Long creditProfileId, LocalDate from, LocalDate to);
    Payment save(Payment payment);
}
