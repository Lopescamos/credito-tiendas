package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.PaymentRepository;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers.PaymentPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories.PaymentPersistenceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentPersistenceRepository persistenceRepository;

    public PaymentRepositoryImpl(PaymentPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return persistenceRepository.findById(id).map(PaymentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Payment> findAllByCreditProfileId(Long creditProfileId) {
        return persistenceRepository.findAllByCreditProfileIdOrderByPaymentDateDescIdDesc(creditProfileId).stream()
                .map(PaymentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public BigDecimal sumAmountByCreditProfileIdInRange(Long creditProfileId, LocalDate from, LocalDate to) {
        var sum = persistenceRepository.sumAmountByProfileIdInRange(creditProfileId, from, to);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    public Payment save(Payment payment) {
        var saved = persistenceRepository.save(PaymentPersistenceAssembler.toPersistenceFromDomain(payment));
        return PaymentPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
