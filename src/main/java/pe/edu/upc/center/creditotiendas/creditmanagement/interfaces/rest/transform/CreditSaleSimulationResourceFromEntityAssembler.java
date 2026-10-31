package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleSimulationResource;

public class CreditSaleSimulationResourceFromEntityAssembler {
    public static CreditSaleSimulationResource toResourceFromEntity(CreditSale sale) {
        return new CreditSaleSimulationResource(
                CreditSaleResourceFromEntityAssembler.toResourceFromEntity(sale),
                PaymentPlanResourceFromEntityAssembler.toResourceFromEntity(sale.paymentPlan()));
    }
}
