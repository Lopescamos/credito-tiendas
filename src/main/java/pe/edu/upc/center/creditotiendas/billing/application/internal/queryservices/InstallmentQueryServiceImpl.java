package pe.edu.upc.center.creditotiendas.billing.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.center.creditotiendas.billing.application.queryservices.InstallmentQueryService;
import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetInstallmentsByCreditSaleIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetInstallmentsDueTodayByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetPaymentDueByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentPaymentDue;
import pe.edu.upc.center.creditotiendas.billing.domain.repositories.InstallmentRepository;
import pe.edu.upc.center.creditotiendas.creditmanagement.application.queryservices.CreditSaleQueryService;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.queries.GetCreditSaleByIdQuery;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.services.LateFeeCalculator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class InstallmentQueryServiceImpl implements InstallmentQueryService {

    private final InstallmentRepository installmentRepository;
    private final CreditSaleQueryService creditSaleQueryService;

    public InstallmentQueryServiceImpl(InstallmentRepository installmentRepository,
                                       CreditSaleQueryService creditSaleQueryService) {
        this.installmentRepository = installmentRepository;
        this.creditSaleQueryService = creditSaleQueryService;
    }

    @Override
    public List<Installment> handle(GetInstallmentsByCreditSaleIdQuery query) {
        return installmentRepository.findAllByCreditSaleId(query.creditSaleId());
    }

    @Override
    public List<Installment> handle(GetInstallmentsDueTodayByStoreIdQuery query) {
        return installmentRepository.findAllDueByStoreIdAndDate(query.storeId(), query.asOf());
    }

    @Override
    public List<InstallmentPaymentDue> handle(GetPaymentDueByCreditProfileIdQuery query) {
        var installments = query.installmentIds() == null || query.installmentIds().isEmpty()
                ? installmentRepository.findAllUnpaidByCreditProfileId(query.creditProfileId())
                : installmentRepository.findAllByIds(query.installmentIds()).stream()
                        .filter(i -> i.getCreditProfileId().equals(query.creditProfileId()) && !i.getStatus().isPaid())
                        .toList();
        var lateRateBySale = new HashMap<Long, pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects.LateInterestRate>();
        return installments.stream().map(installment -> {
            var lateRate = lateRateBySale.computeIfAbsent(installment.getCreditSaleId(),
                    saleId -> creditSaleQueryService.handle(new GetCreditSaleByIdQuery(saleId))
                            .orElseThrow().getLateInterestRateSnapshot());
            var fee = LateFeeCalculator.calculate(installment.getFixedInstallment(), lateRate,
                    installment.getDueDate(), query.asOf());
            var days = LateFeeCalculator.daysLate(installment.getDueDate(), query.asOf());
            var total = fee.add(installment.getInterest()).add(installment.getCapital())
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            return new InstallmentPaymentDue(installment.getId(), installment.getNumber(), installment.getDueDate(),
                    days, fee, installment.getInterest(), installment.getCapital(), total);
        }).toList();
    }
}
