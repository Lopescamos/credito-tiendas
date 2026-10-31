package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditSale;
import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.entities.SaleDetail;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditSalePersistenceEntity;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.SaleDetailPersistenceEntity;

/**
 * Maps the {@code CreditSale} aggregate, including its {@code SaleDetail} children and rate snapshot.
 */
public final class CreditSalePersistenceAssembler {

    private CreditSalePersistenceAssembler() {
    }

    public static CreditSale toDomainFromPersistence(CreditSalePersistenceEntity entity) {
        if (entity == null) return null;
        var sale = new CreditSale();
        sale.setId(entity.getId());
        sale.setCreditProfileId(entity.getCreditProfileId());
        sale.setSaleDate(entity.getSaleDate());
        sale.setTotalAmount(entity.getTotalAmount());
        sale.setModality(entity.getModality());
        sale.setTermDays(entity.getTermDays());
        sale.setGraceDays(entity.getGraceDays());
        sale.setInstallmentCount(entity.getInstallmentCount());
        sale.setFixedInstallment(entity.getFixedInstallment());
        sale.setFinancedCapital(entity.getFinancedCapital());
        sale.setInterestRateSnapshot(InterestRatePersistenceAssembler.toDomainFromPersistence(entity.getInterestRateSnapshot()));
        sale.setLateInterestRateSnapshot(InterestRatePersistenceAssembler.toDomainFromPersistence(entity.getLateInterestRateSnapshot()));
        sale.setStatus(entity.getStatus());
        sale.setOriginPurchaseRequestId(entity.getOriginPurchaseRequestId());
        sale.setDetails(entity.getDetails().stream().map(CreditSalePersistenceAssembler::toDomainFromPersistence).toList());
        return sale;
    }

    public static CreditSalePersistenceEntity toPersistenceFromDomain(CreditSale sale) {
        if (sale == null) return null;
        var entity = new CreditSalePersistenceEntity();
        if (sale.getId() != null) entity.setId(sale.getId());
        entity.setCreditProfileId(sale.getCreditProfileId());
        entity.setSaleDate(sale.getSaleDate());
        entity.setTotalAmount(sale.getTotalAmount());
        entity.setModality(sale.getModality());
        entity.setTermDays(sale.getTermDays());
        entity.setGraceDays(sale.getGraceDays());
        entity.setInstallmentCount(sale.getInstallmentCount());
        entity.setFixedInstallment(sale.getFixedInstallment());
        entity.setFinancedCapital(sale.getFinancedCapital());
        entity.setInterestRateSnapshot(InterestRatePersistenceAssembler.toPersistenceFromDomain(sale.getInterestRateSnapshot()));
        entity.setLateInterestRateSnapshot(InterestRatePersistenceAssembler.toPersistenceFromDomain(sale.getLateInterestRateSnapshot()));
        entity.setStatus(sale.getStatus());
        entity.setOriginPurchaseRequestId(sale.getOriginPurchaseRequestId());
        sale.getDetails().forEach(detail -> entity.getDetails().add(toPersistenceFromDomain(detail, entity)));
        return entity;
    }

    private static SaleDetail toDomainFromPersistence(SaleDetailPersistenceEntity entity) {
        var detail = new SaleDetail();
        detail.setId(entity.getId());
        detail.setProductId(entity.getProductId());
        detail.setQuantity(entity.getQuantity());
        detail.setUnitPrice(entity.getUnitPrice());
        detail.setSubtotal(entity.getSubtotal());
        return detail;
    }

    private static SaleDetailPersistenceEntity toPersistenceFromDomain(SaleDetail detail, CreditSalePersistenceEntity sale) {
        var entity = new SaleDetailPersistenceEntity();
        if (detail.getId() != null) entity.setId(detail.getId());
        entity.setCreditSale(sale);
        entity.setProductId(detail.getProductId());
        entity.setQuantity(detail.getQuantity());
        entity.setUnitPrice(detail.getUnitPrice());
        entity.setSubtotal(detail.getSubtotal());
        return entity;
    }
}
