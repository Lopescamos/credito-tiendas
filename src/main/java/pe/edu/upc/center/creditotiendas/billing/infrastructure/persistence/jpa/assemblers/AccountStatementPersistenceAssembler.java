package pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.billing.domain.model.aggregates.AccountStatement;
import pe.edu.upc.center.creditotiendas.billing.domain.model.entities.AccountStatementDetail;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.AccountStatementDetailPersistenceEntity;
import pe.edu.upc.center.creditotiendas.billing.infrastructure.persistence.jpa.entities.AccountStatementPersistenceEntity;

import java.time.YearMonth;

public final class AccountStatementPersistenceAssembler {

    private AccountStatementPersistenceAssembler() {
    }

    public static AccountStatement toDomainFromPersistence(AccountStatementPersistenceEntity entity) {
        if (entity == null) return null;
        var statement = new AccountStatement();
        statement.setId(entity.getId());
        statement.setCreditProfileId(entity.getCreditProfileId());
        statement.setPeriod(YearMonth.parse(entity.getPeriod()));
        statement.setCutoffDate(entity.getCutoffDate());
        statement.setPreviousBalance(entity.getPreviousBalance());
        statement.setPeriodPurchases(entity.getPeriodPurchases());
        statement.setPeriodPayments(entity.getPeriodPayments());
        statement.setPeriodInterest(entity.getPeriodInterest());
        statement.setPeriodLateFees(entity.getPeriodLateFees());
        statement.setFinalBalance(entity.getFinalBalance());
        statement.setDetails(entity.getDetails().stream()
                .map(AccountStatementPersistenceAssembler::toDomainFromPersistence)
                .toList());
        return statement;
    }

    public static AccountStatementPersistenceEntity toPersistenceFromDomain(AccountStatement statement) {
        if (statement == null) return null;
        var entity = new AccountStatementPersistenceEntity();
        if (statement.getId() != null) entity.setId(statement.getId());
        entity.setCreditProfileId(statement.getCreditProfileId());
        entity.setPeriod(statement.getPeriod().toString());
        entity.setCutoffDate(statement.getCutoffDate());
        entity.setPreviousBalance(statement.getPreviousBalance());
        entity.setPeriodPurchases(statement.getPeriodPurchases());
        entity.setPeriodPayments(statement.getPeriodPayments());
        entity.setPeriodInterest(statement.getPeriodInterest());
        entity.setPeriodLateFees(statement.getPeriodLateFees());
        entity.setFinalBalance(statement.getFinalBalance());
        statement.getDetails().forEach(detail ->
                entity.getDetails().add(toPersistenceFromDomain(detail, entity)));
        return entity;
    }

    private static AccountStatementDetail toDomainFromPersistence(AccountStatementDetailPersistenceEntity entity) {
        var detail = new AccountStatementDetail();
        detail.setId(entity.getId());
        detail.setMovementType(entity.getMovementType());
        detail.setDescription(entity.getDescription());
        detail.setAmount(entity.getAmount());
        detail.setDate(entity.getDate());
        return detail;
    }

    private static AccountStatementDetailPersistenceEntity toPersistenceFromDomain(AccountStatementDetail detail, AccountStatementPersistenceEntity statement) {
        var entity = new AccountStatementDetailPersistenceEntity();
        if (detail.getId() != null) entity.setId(detail.getId());
        entity.setAccountStatement(statement);
        entity.setMovementType(detail.getMovementType());
        entity.setDescription(detail.getDescription());
        entity.setAmount(detail.getAmount());
        entity.setDate(detail.getDate());
        return entity;
    }
}
