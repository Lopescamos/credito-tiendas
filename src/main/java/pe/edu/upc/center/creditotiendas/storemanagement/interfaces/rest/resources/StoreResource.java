package pe.edu.upc.center.creditotiendas.storemanagement.interfaces.rest.resources;

public record StoreResource(
        Long id,
        String ruc,
        String legalName,
        String tradeName,
        String address,
        String phone,
        boolean active) {
}
