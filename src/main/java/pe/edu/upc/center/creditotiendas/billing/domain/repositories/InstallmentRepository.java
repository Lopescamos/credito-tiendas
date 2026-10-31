package pe.edu.upc.center.creditotiendas.billing.domain.repositories;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence port of the {@link Installment} aggregate.
 */
public interface InstallmentRepository {
    Optional<Installment> findById(Long id);
    List<Installment> findAllByIds(List<Long> ids);
    List<Installment> findAllByCreditSaleId(Long creditSaleId);
    List<Installment> findAllUnpaidByCreditProfileId(Long creditProfileId);
    List<Installment> findAllByCreditProfileIdInRange(Long creditProfileId, LocalDate from, LocalDate to);
    List<Installment> findAllPendingBeforeDueDate(LocalDate asOf);
    List<Installment> findAllDueByStoreIdAndDate(Long storeId, LocalDate asOf);
    boolean existsAnyUnpaidByCreditSaleId(Long creditSaleId);
    boolean existsAnyPaidLateByCreditSaleId(Long creditSaleId);
    Installment save(Installment installment);
    List<Installment> saveAll(List<Installment> installments);
}
