package pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.assemblers;

import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.MeasurementUnit;
import pe.edu.upc.center.creditotiendas.storemanagement.infrastructure.persistence.jpa.entities.MeasurementUnitPersistenceEntity;

public final class MeasurementUnitPersistenceAssembler {

    private MeasurementUnitPersistenceAssembler() {
    }

    public static MeasurementUnit toDomainFromPersistence(MeasurementUnitPersistenceEntity entity) {
        if (entity == null) return null;
        var unit = new MeasurementUnit();
        unit.setId(entity.getId());
        unit.setName(entity.getName());
        unit.setAbbreviation(entity.getAbbreviation());
        return unit;
    }

    public static MeasurementUnitPersistenceEntity toPersistenceFromDomain(MeasurementUnit unit) {
        if (unit == null) return null;
        var entity = new MeasurementUnitPersistenceEntity();
        if (unit.getId() != null) entity.setId(unit.getId());
        entity.setName(unit.getName());
        entity.setAbbreviation(unit.getAbbreviation());
        return entity;
    }
}
