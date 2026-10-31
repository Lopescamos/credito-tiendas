package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.LateFee;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.LateFeeRepository;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers.LateFeePersistenceAssembler;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories.InstallmentPersistenceRepository;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories.LateFeePersistenceRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public class LateFeeRepositoryImpl implements LateFeeRepository {

    private final LateFeePersistenceRepository lateFeePersistenceRepository;
    private final InstallmentPersistenceRepository installmentPersistenceRepository;

    public LateFeeRepositoryImpl(LateFeePersistenceRepository lateFeePersistenceRepository,
                                 InstallmentPersistenceRepository installmentPersistenceRepository) {
        this.lateFeePersistenceRepository = lateFeePersistenceRepository;
        this.installmentPersistenceRepository = installmentPersistenceRepository;
    }

    @Override
    public LateFee save(LateFee lateFee) {
        var installment = installmentPersistenceRepository.findById(lateFee.getInstallmentId())
                .orElseThrow(() -> new IllegalStateException(
                        "[LateFee] installment %d does not exist".formatted(lateFee.getInstallmentId())));
        var saved = lateFeePersistenceRepository.save(LateFeePersistenceAssembler.toPersistenceFromDomain(lateFee, installment));
        return LateFeePersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public List<LateFee> findAllByInstallmentId(Long installmentId) {
        return lateFeePersistenceRepository.findAllByInstallmentIdOrderByCalculatedAtDesc(installmentId).stream()
                .map(LateFeePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public BigDecimal sumLatestByCreditProfileIdInRange(Long creditProfileId, Instant from, Instant to) {
        var sum = lateFeePersistenceRepository.sumLatestByProfileIdInRange(creditProfileId, from, to);
        return sum == null ? BigDecimal.ZERO : sum;
    }
}
