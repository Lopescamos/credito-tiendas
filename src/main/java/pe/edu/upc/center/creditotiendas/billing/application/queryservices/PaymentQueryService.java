package pe.edu.upc.center.creditotiendas.billing.application.queryservices;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.queries.GetPaymentsByCreditProfileIdQuery;

import java.util.List;

public interface PaymentQueryService {
    List<Payment> handle(GetPaymentsByCreditProfileIdQuery query);
}
