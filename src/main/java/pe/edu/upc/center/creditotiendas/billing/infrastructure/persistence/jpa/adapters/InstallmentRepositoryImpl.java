package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentStatuses;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.InstallmentRepository;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers.InstallmentPersistenceAssembler;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.InstallmentPersistenceEntity;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.repositories.InstallmentPersistenceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class InstallmentRepositoryImpl implements InstallmentRepository {

    private final InstallmentPersistenceRepository persistenceRepository;

    public InstallmentRepositoryImpl(InstallmentPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<Installment> findById(Long id) {
        return persistenceRepository.findById(id).map(InstallmentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Installment> findAllByIds(List<Long> ids) {
        return toDomain(persistenceRepository.findAllById(ids));
    }

    @Override
    public List<Installment> findAllByCreditSaleId(Long creditSaleId) {
        return toDomain(persistenceRepository.findAllByCreditSaleIdOrderByNumberAsc(creditSaleId));
    }

    @Override
    public List<Installment> findAllUnpaidByCreditProfileId(Long creditProfileId) {
        return toDomain(persistenceRepository.findAllUnpaidByProfileId(creditProfileId));
    }

    @Override
    public List<Installment> findAllByCreditProfileIdInRange(Long creditProfileId, LocalDate from, LocalDate to) {
        return toDomain(persistenceRepository
                .findAllByCreditProfileIdAndDueDateBetweenOrderByDueDateAsc(creditProfileId, from, to));
    }

    @Override
    public List<Installment> findAllPendingBeforeDueDate(LocalDate asOf) {
        return toDomain(persistenceRepository.findAllByStatusAndDueDateBefore(InstallmentStatuses.PENDING, asOf));
    }

    @Override
    public List<Installment> findAllDueByStoreIdAndDate(Long storeId, LocalDate asOf) {
        return toDomain(persistenceRepository.findAllDueByStoreIdAndDate(storeId, asOf));
    }

    @Override
    public boolean existsAnyUnpaidByCreditSaleId(Long creditSaleId) {
        return persistenceRepository.existsByCreditSaleIdAndStatusIn(creditSaleId,
                List.of(InstallmentStatuses.PENDING, InstallmentStatuses.OVERDUE));
    }

    @Override
    public boolean existsAnyPaidLateByCreditSaleId(Long creditSaleId) {
        return persistenceRepository.existsByCreditSaleIdAndStatusIn(creditSaleId,
                List.of(InstallmentStatuses.PAID_LATE));
    }

    @Override
    public Installment save(Installment installment) {
        var saved = persistenceRepository.save(InstallmentPersistenceAssembler.toPersistenceFromDomain(installment));
        return InstallmentPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public List<Installment> saveAll(List<Installment> installments) {
        var entities = installments.stream()
                .map(InstallmentPersistenceAssembler::toPersistenceFromDomain)
                .toList();
        return toDomain(persistenceRepository.saveAll(entities));
    }

    private static List<Installment> toDomain(List<InstallmentPersistenceEntity> entities) {
        return entities.stream().map(InstallmentPersistenceAssembler::toDomainFromPersistence).toList();
    }
}
