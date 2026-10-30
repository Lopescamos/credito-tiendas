package pe.edu.upc.center.creditotiendas.iam.interfaces.acl;

/**
 * Public face of the IAM context for other bounded contexts. Uses primitive types only so the
 * IAM model never leaks: "not found" is {@code 0L} or an empty string.
 */
public interface IamContextFacade {

    /**
     * Resolves the id of a user, typically the authenticated one ({@code Authentication.getName()}).
     *
     * @return the user id, or {@code 0L} if no user has that email
     */
    Long fetchUserIdByEmail(String email);

    /**
     * @return the role name ({@code SYSTEM_ADMIN}, {@code STORE_ADMIN}, {@code CLIENT}),
     *         or an empty string if the user does not exist
     */
    String fetchUserRoleByUserId(Long userId);

    /**
     * Multi-tenant check for store admins (US-01): whether the user is active and actively
     * linked to the store.
     */
    boolean isUserAssignedToStore(Long userId, Long storeId);
}
