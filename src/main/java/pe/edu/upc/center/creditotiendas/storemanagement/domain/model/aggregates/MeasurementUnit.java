package pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.center.creditotiendas.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Unit in which a catalog product is sold (table {@code measurement_units}, model: Unidad_Medida).
 * Seeded at startup; there is no API to create or edit units.
 */
@Getter
public class MeasurementUnit extends AbstractDomainAggregateRoot<MeasurementUnit> {

    @Setter private Long id;
    @Setter private String name;
    @Setter private String abbreviation;

    public MeasurementUnit() {
        this.name = "";
        this.abbreviation = "";
    }

    public MeasurementUnit(String name, String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
    }
}
