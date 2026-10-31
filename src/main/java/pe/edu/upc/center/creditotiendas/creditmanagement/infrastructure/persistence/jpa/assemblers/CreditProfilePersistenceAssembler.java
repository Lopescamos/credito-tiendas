package pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.aggregates.CreditProfile;
import pe.edu.upc.center.creditotiendas.creditmanagement.infrastructure.persistence.jpa.entities.CreditProfilePersistenceEntity;

public final class CreditProfilePersistenceAssembler {

    private CreditProfilePersistenceAssembler() {
    }

    public static CreditProfile toDomainFromPersistence(CreditProfilePersistenceEntity entity) {
        if (entity == null) return null;
        var profile = new CreditProfile();
        profile.setId(entity.getId());
        profile.setUserId(entity.getUserId());
        profile.setStoreId(entity.getStoreId());
        profile.setInterestRate(InterestRatePersistenceAssembler.toDomainFromPersistence(entity.getInterestRate()));
        profile.setLateInterestRate(InterestRatePersistenceAssembler.toDomainFromPersistence(entity.getLateInterestRate()));
        profile.setCurrencyId(entity.getCurrencyId());
        profile.setCreditLimit(entity.getCreditLimit());
        profile.setMaxTermDays(entity.getMaxTermDays());
        profile.setCutoffDay(entity.getCutoffDay());
        profile.setPaymentDay(entity.getPaymentDay());
        profile.setActive(entity.isActive());
        return profile;
    }

    public static CreditProfilePersistenceEntity toPersistenceFromDomain(CreditProfile profile) {
        if (profile == null) return null;
        var entity = new CreditProfilePersistenceEntity();
        if (profile.getId() != null) entity.setId(profile.getId());
        entity.setUserId(profile.getUserId());
        entity.setStoreId(profile.getStoreId());
        entity.setInterestRate(InterestRatePersistenceAssembler.toPersistenceFromDomain(profile.getInterestRate()));
        entity.setLateInterestRate(InterestRatePersistenceAssembler.toPersistenceFromDomain(profile.getLateInterestRate()));
        entity.setCurrencyId(profile.getCurrencyId());
        entity.setCreditLimit(profile.getCreditLimit());
        entity.setMaxTermDays(profile.getMaxTermDays());
        entity.setCutoffDay(profile.getCutoffDay());
        entity.setPaymentDay(profile.getPaymentDay());
        entity.setActive(profile.isActive());
        return entity;
    }
}
