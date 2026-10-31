package pe.edu.upc.center.creditotiendas.billing.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import pe.edu.upc.center.creditotiendas.billing.application.commandservices.PaymentCommandService;
import pe.edu.upc.center.creditotiendas.billing.application.internal.outboundservices.acl.ExternalCreditService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.LateFee;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.center.creditotiendas.billing.domain.model.entities.PaymentAllocation;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.InstallmentRepository;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.LateFeeRepository;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.PaymentRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditSaleQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.InstallmentDue;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.LateFeeCalculator;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.PaymentAllocationPolicy;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;

import java.math.BigDecimal;
import java.util.*;

/**
 * Registers a customer payment (US-05, US-10). Reuses the pure domain services of Credit:
 * {@link LateFeeCalculator} for the late fee of each overdue installment and
 * {@link PaymentAllocationPolicy} for the mandatory prelation and the no-partial-payment rule.
 *
 * <p>A single payment covers installments of a single credit sale, so the late rate (from the sale snapshot)
 * is the same for every installment of the payment. Paying installments of several sales is modeled as
 * several payments.</p>
 */
@Slf4j
@Service
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final InstallmentRepository installmentRepository;
    private final LateFeeRepository lateFeeRepository;
    private final CreditSaleQueryService creditSaleQueryService;
    private final ExternalCreditService externalCreditService;

    public PaymentCommandServiceImpl(PaymentRepository paymentRepository, InstallmentRepository installmentRepository,
                                     LateFeeRepository lateFeeRepository,
                                     CreditSaleQueryService creditSaleQueryService,
                                     ExternalCreditService externalCreditService) {
        this.paymentRepository = paymentRepository;
        this.installmentRepository = installmentRepository;
        this.lateFeeRepository = lateFeeRepository;
        this.creditSaleQueryService = creditSaleQueryService;
        this.externalCreditService = externalCreditService;
    }

    /**
     * Checks, in order: every installment exists, belongs to the profile, is unpaid and shares the same sale;
     * then uses the sale's rate snapshot to build the amount owed at {@code paymentDate} and asks the policy
     * to allocate the full payment. On success it persists the payment with its allocations, snapshots the
     * late fee of every installment that was late, flips every installment to paid, and asks Credit to move
     * the sale to {@code PAID_ON_TIME} or {@code PAID_LATE} when every installment of the sale is paid.
     */
    @Override
    @Transactional
    public Result<Payment, ApplicationError> handle(RegisterPaymentCommand command) {
        var installments = new ArrayList<>(installmentRepository.findAllByIds(command.installmentIds()));
        if (installments.size() != command.installmentIds().size())
            return Result.failure(ApplicationError.notFound("Installment", command.installmentIds().toString()));

        var saleIds = installments.stream().map(Installment::getCreditSaleId).collect(java.util.stream.Collectors.toSet());
        if (saleIds.size() != 1)
            return Result.failure(ApplicationError.businessRuleViolation("Payment",
                    "A single payment must cover installments of one credit sale only; got sales %s".formatted(saleIds)));
        for (var installment : installments) {
            if (!Objects.equals(installment.getCreditProfileId(), command.creditProfileId()))
                return Result.failure(ApplicationError.businessRuleViolation("Installment",
                        "Installment %d does not belong to credit profile %d".formatted(installment.getId(), command.creditProfileId())));
            if (installment.getStatus().isPaid())
                return Result.failure(ApplicationError.businessRuleViolation("Installment",
                        "Installment %d is already paid".formatted(installment.getId())));
        }
        var saleId = saleIds.iterator().next();
        var sale = creditSaleQueryService.handle(new GetCreditSaleByIdQuery(saleId));
        if (sale.isEmpty())
            return Result.failure(ApplicationError.notFound("CreditSale", saleId.toString()));
        var lateRate = sale.get().getLateInterestRateSnapshot();

        // Compute late fee per installment at the payment date and build the dues list
        var feeByInstallmentId = new HashMap<Long, BigDecimal>();
        var daysLateByInstallmentId = new HashMap<Long, Integer>();
        var dues = new ArrayList<InstallmentDue>();
        installments.sort(Comparator.comparingInt(Installment::getNumber));
        for (var installment : installments) {
            var fee = LateFeeCalculator.calculate(installment.getFixedInstallment(), lateRate,
                    installment.getDueDate(), command.paymentDate());
            var days = LateFeeCalculator.daysLate(installment.getDueDate(), command.paymentDate());
            feeByInstallmentId.put(installment.getId(), fee);
            daysLateByInstallmentId.put(installment.getId(), days);
            dues.add(new InstallmentDue(installment.getNumber(), fee, installment.getInterest(), installment.getCapital()));
        }

        var owed = PaymentAllocationPolicy.totalOwed(dues);
        List<pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.PaymentAllocation> creditAllocations;
        try {
            creditAllocations = PaymentAllocationPolicy.allocate(dues, owed);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.businessRuleViolation("Payment", e.getMessage()));
        }

        // Map Credit's allocation records (keyed by installment number, unique inside one sale) back to our ids
        var installmentIdByNumber = new HashMap<Integer, Long>();
        installments.forEach(i -> installmentIdByNumber.put(i.getNumber(), i.getId()));
        var billingAllocationsByInstallmentId = new LinkedHashMap<Long, PaymentAllocation>();
        for (var creditAllocation : creditAllocations) {
            var installmentId = installmentIdByNumber.get(creditAllocation.installmentNumber());
            var allocation = billingAllocationsByInstallmentId.computeIfAbsent(installmentId,
                    id -> new PaymentAllocation(id, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            switch (creditAllocation.component()) {
                case LATE_FEE -> allocation.setLateFeeAmount(creditAllocation.amount());
                case INTEREST -> allocation.setInterestAmount(creditAllocation.amount());
                case CAPITAL -> allocation.setCapitalAmount(creditAllocation.amount());
            }
        }

        try {
            var payment = new Payment(command.creditProfileId(), owed, command.paymentDate(), command.paymentMethod(),
                    command.reference(), new ArrayList<>(billingAllocationsByInstallmentId.values()));
            var saved = paymentRepository.save(payment);

            for (var installment : installments) {
                var fee = feeByInstallmentId.get(installment.getId());
                var days = daysLateByInstallmentId.get(installment.getId());
                if (days > 0 && fee.signum() > 0)
                    lateFeeRepository.save(new LateFee(installment.getId(), days, fee));
                installment.markPaid(command.paymentDate());
            }
            installmentRepository.saveAll(installments);

            updateSaleStatusIfFullyPaid(saleId);
            return Result.success(saved);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Error in register-payment", e);
            return Result.failure(ApplicationError.unexpected("register-payment", "The payment could not be registered"));
        }
    }

    private void updateSaleStatusIfFullyPaid(Long saleId) {
        if (installmentRepository.existsAnyUnpaidByCreditSaleId(saleId)) return;
        if (installmentRepository.existsAnyPaidLateByCreditSaleId(saleId))
            externalCreditService.markCreditSaleAsPaidLate(saleId);
        else
            externalCreditService.markCreditSaleAsPaidOnTime(saleId);
    }
}
