package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources;

import java.math.BigDecimal;

public record ProductResource(
        Long id,
        Long storeId,
        String name,
        String description,
        String type,
        String supplier,
        String brand,
        Long measurementUnitId,
        String imageUrl,
        BigDecimal cashPrice,
        BigDecimal creditPrice,
        boolean active) {
}
