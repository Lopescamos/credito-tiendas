package pe.edu.upc.center.creditotiendas.iam.domain.model.valueobjects;

/**
 * Roles available in the platform. Each user has exactly one role.
 *
 * <p>Persisted by name ({@code EnumType.STRING}), so reordering the constants never
 * corrupts existing rows.</p>
 */
public enum Roles {
    SYSTEM_ADMIN("Manages stores, users and the operations log"),
    STORE_ADMIN("Manages one or more stores: catalog, customers, credit sales and payments"),
    CLIENT("Customer with credit profiles in one or more stores");

    private final String description;

    Roles(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Whether a system admin can create users with this role through user management (US-14).
     * The system admin account itself is only created by seeding at startup.
     */
    public boolean isAssignableThroughUserManagement() {
        return this != SYSTEM_ADMIN;
    }
}
