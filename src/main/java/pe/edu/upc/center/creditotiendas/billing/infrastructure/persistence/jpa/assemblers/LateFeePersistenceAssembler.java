package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.LateFee;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.InstallmentPersistenceEntity;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.LateFeePersistenceEntity;

public final class LateFeePersistenceAssembler {

    private LateFeePersistenceAssembler() {
    }

    public static LateFee toDomainFromPersistence(LateFeePersistenceEntity entity) {
        if (entity == null) return null;
        var fee = new LateFee();
        fee.setId(entity.getId());
        fee.setInstallmentId(entity.getInstallment().getId());
        fee.setDaysLate(entity.getDaysLate());
        fee.setAmount(entity.getAmount());
        fee.setCalculatedAt(entity.getCalculatedAt());
        return fee;
    }

    public static LateFeePersistenceEntity toPersistenceFromDomain(LateFee fee, InstallmentPersistenceEntity installment) {
        if (fee == null) return null;
        var entity = new LateFeePersistenceEntity();
        if (fee.getId() != null) entity.setId(fee.getId());
        entity.setInstallment(installment);
        entity.setDaysLate(fee.getDaysLate());
        entity.setAmount(fee.getAmount());
        entity.setCalculatedAt(fee.getCalculatedAt());
        return entity;
    }
}
