package pe.edu.upc.center.creditotiendas.billing.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.InstallmentCommandService;
import pe.edu.upc.center.creditotiendas.billing.application.internal.outboundservices.acl.ExternalCreditService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.CreateInstallmentScheduleCommand;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.MarkOverdueInstallmentsCommand;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.InstallmentRepository;

import java.util.HashSet;

/**
 * Installment lifecycle driven by Billing (US-07).
 */
@Slf4j
@Service
public class InstallmentCommandServiceImpl implements InstallmentCommandService {

    private final InstallmentRepository installmentRepository;
    private final ExternalCreditService externalCreditService;

    public InstallmentCommandServiceImpl(InstallmentRepository installmentRepository,
                                         ExternalCreditService externalCreditService) {
        this.installmentRepository = installmentRepository;
        this.externalCreditService = externalCreditService;
    }

    /**
     * Creates one row per scheduled installment of the sale. Idempotent: if cuotas already exist for the sale,
     * returns 0 and keeps them (prevents duplicates when the event is re-published).
     */
    @Override
    public int handle(CreateInstallmentScheduleCommand command) {
        if (!installmentRepository.findAllByCreditSaleId(command.creditSaleId()).isEmpty()) {
            log.warn("Installments for sale {} already exist, skipping", command.creditSaleId());
            return 0;
        }
        var installments = command.installments().stream()
                .map(scheduled -> new Installment(command.creditSaleId(), command.creditProfileId(),
                        scheduled.number(), scheduled.dueDate(), scheduled.capital(), scheduled.interest(),
                        scheduled.amount(), scheduled.remainingBalance()))
                .toList();
        installmentRepository.saveAll(installments);
        log.info("Billing created {} installments for sale {}", installments.size(), command.creditSaleId());
        return installments.size();
    }

    /**
     * Flips {@code PENDING} installments whose due date is before {@code asOf} to {@code OVERDUE} and asks
     * Credit to move each affected sale to {@code OVERDUE} too.
     */
    @Override
    public int handle(MarkOverdueInstallmentsCommand command) {
        var newlyOverdue = installmentRepository.findAllPendingBeforeDueDate(command.asOf());
        if (newlyOverdue.isEmpty()) return 0;
        newlyOverdue.forEach(installment -> installment.markOverdueIfLate(command.asOf()));
        installmentRepository.saveAll(newlyOverdue);
        var affectedSales = new HashSet<Long>();
        newlyOverdue.forEach(installment -> affectedSales.add(installment.getCreditSaleId()));
        affectedSales.forEach(externalCreditService::markCreditSaleAsOverdue);
        log.info("Billing marked {} installments as OVERDUE across {} sales", newlyOverdue.size(), affectedSales.size());
        return newlyOverdue.size();
    }
}
