package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Installment;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.InstallmentPersistenceEntity;

public final class InstallmentPersistenceAssembler {

    private InstallmentPersistenceAssembler() {
    }

    public static Installment toDomainFromPersistence(InstallmentPersistenceEntity entity) {
        if (entity == null) return null;
        var installment = new Installment();
        installment.setId(entity.getId());
        installment.setCreditSaleId(entity.getCreditSaleId());
        installment.setCreditProfileId(entity.getCreditProfileId());
        installment.setNumber(entity.getNumber());
        installment.setDueDate(entity.getDueDate());
        installment.setCapital(entity.getCapital());
        installment.setInterest(entity.getInterest());
        installment.setFixedInstallment(entity.getFixedInstallment());
        installment.setRemainingBalance(entity.getRemainingBalance());
        installment.setStatus(entity.getStatus());
        installment.setPaymentDate(entity.getPaymentDate());
        return installment;
    }

    public static InstallmentPersistenceEntity toPersistenceFromDomain(Installment installment) {
        if (installment == null) return null;
        var entity = new InstallmentPersistenceEntity();
        if (installment.getId() != null) entity.setId(installment.getId());
        entity.setCreditSaleId(installment.getCreditSaleId());
        entity.setCreditProfileId(installment.getCreditProfileId());
        entity.setNumber(installment.getNumber());
        entity.setDueDate(installment.getDueDate());
        entity.setCapital(installment.getCapital());
        entity.setInterest(installment.getInterest());
        entity.setFixedInstallment(installment.getFixedInstallment());
        entity.setRemainingBalance(installment.getRemainingBalance());
        entity.setStatus(installment.getStatus());
        entity.setPaymentDate(installment.getPaymentDate());
        return entity;
    }
}
