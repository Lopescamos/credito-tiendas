package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.Payment;
import pe.edu.upc.center.creditotiendas.billing.domain.model.entities.PaymentAllocation;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.PaymentAllocationPersistenceEntity;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.PaymentPersistenceEntity;

public final class PaymentPersistenceAssembler {

    private PaymentPersistenceAssembler() {
    }

    public static Payment toDomainFromPersistence(PaymentPersistenceEntity entity) {
        if (entity == null) return null;
        var payment = new Payment();
        payment.setId(entity.getId());
        payment.setCreditProfileId(entity.getCreditProfileId());
        payment.setTotalAmount(entity.getTotalAmount());
        payment.setPaymentDate(entity.getPaymentDate());
        payment.setPaymentMethod(entity.getPaymentMethod());
        payment.setReference(entity.getReference());
        payment.setRegisteredAt(entity.getRegisteredAt());
        payment.setAllocations(entity.getAllocations().stream()
                .map(PaymentPersistenceAssembler::toDomainFromPersistence)
                .toList());
        return payment;
    }

    public static PaymentPersistenceEntity toPersistenceFromDomain(Payment payment) {
        if (payment == null) return null;
        var entity = new PaymentPersistenceEntity();
        if (payment.getId() != null) entity.setId(payment.getId());
        entity.setCreditProfileId(payment.getCreditProfileId());
        entity.setTotalAmount(payment.getTotalAmount());
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setPaymentMethod(payment.getPaymentMethod());
        entity.setReference(payment.getReference());
        entity.setRegisteredAt(payment.getRegisteredAt());
        payment.getAllocations().forEach(allocation ->
                entity.getAllocations().add(toPersistenceFromDomain(allocation, entity)));
        return entity;
    }

    private static PaymentAllocation toDomainFromPersistence(PaymentAllocationPersistenceEntity entity) {
        var allocation = new PaymentAllocation();
        allocation.setId(entity.getId());
        allocation.setInstallmentId(entity.getInstallmentId());
        allocation.setLateFeeAmount(entity.getLateFeeAmount());
        allocation.setInterestAmount(entity.getInterestAmount());
        allocation.setCapitalAmount(entity.getCapitalAmount());
        return allocation;
    }

    private static PaymentAllocationPersistenceEntity toPersistenceFromDomain(PaymentAllocation allocation, PaymentPersistenceEntity payment) {
        var entity = new PaymentAllocationPersistenceEntity();
        if (allocation.getId() != null) entity.setId(allocation.getId());
        entity.setPayment(payment);
        entity.setInstallmentId(allocation.getInstallmentId());
        entity.setLateFeeAmount(allocation.getLateFeeAmount());
        entity.setInterestAmount(allocation.getInterestAmount());
        entity.setCapitalAmount(allocation.getCapitalAmount());
        return entity;
    }
}
