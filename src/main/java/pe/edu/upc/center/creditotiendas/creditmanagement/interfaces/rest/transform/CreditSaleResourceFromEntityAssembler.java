package pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.transform;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.CreditSaleResource;
import pe.edu.upc.center.creditotiendas.creditmanagement.interfaces.rest.resources.LineItemResource;

public class CreditSaleResourceFromEntityAssembler {
    public static CreditSaleResource toResourceFromEntity(CreditSale sale) {
        var rate = sale.getInterestRateSnapshot();
        var lateRate = sale.getLateInterestRateSnapshot();
        return new CreditSaleResource(
                sale.getId(),
                sale.getCreditProfileId(),
                sale.getSaleDate(),
                sale.getTotalAmount(),
                sale.getModality().name(),
                sale.getTermDays(),
                sale.getGraceDays(),
                sale.getInstallmentCount(),
                sale.getFixedInstallment(),
                sale.getFinancedCapital(),
                rate.percentage(),
                rate.type().name(),
                rate.capitalizationPeriod() == null ? null : rate.capitalizationPeriod().name(),
                lateRate.percentage(),
                lateRate.type().name(),
                sale.getStatus().name(),
                sale.getOriginPurchaseRequestId(),
                sale.getDetails().stream()
                        .map(detail -> new LineItemResource(detail.getProductId(), detail.getQuantity(),
                                detail.getUnitPrice(), detail.getSubtotal()))
                        .toList());
    }
}
