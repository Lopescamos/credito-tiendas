package pe.edu.upc.center.creditotiendas.billing.application.queryservices;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetInstallmentsByCreditSaleIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetInstallmentsDueTodayByStoreIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetPaymentDueByCreditProfileIdQuery;
import pe.edu.upc.center.creditotiendas.billing.domain.model.valueobjects.InstallmentPaymentDue;

import java.util.List;

public interface InstallmentQueryService {
    List<Installment> handle(GetInstallmentsByCreditSaleIdQuery query);
    List<Installment> handle(GetInstallmentsDueTodayByStoreIdQuery query);
    List<InstallmentPaymentDue> handle(GetPaymentDueByCreditProfileIdQuery query);
}
