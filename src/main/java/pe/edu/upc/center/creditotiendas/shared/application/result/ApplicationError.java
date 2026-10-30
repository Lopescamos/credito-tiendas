package pe.edu.upc.center.creditotiendas.shared.application.result;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Represents an error that occurred in the application layer.
 * Designed to be easily mapped to HTTP responses and provide structured error information.
 *
 * @param code     A machine-readable error code (e.g., "CREDIT_SALE_NOT_FOUND", "VALIDATION_ERROR")
 * @param message  A human-readable error message
 * @param details  Optional additional context about the error
 */
@NullMarked
public record ApplicationError(
        String code,
        String message,
        @Nullable String details) {

    /**
     * Creates an ApplicationError with code and message only.
     */
    public ApplicationError(String code, String message) {
        this(code, message, null);
    }

    /**
     * Validation error: input data is invalid or violates constraints
     */
    public static ApplicationError validationError(String fieldOrConcept, String reason) {
        return new ApplicationError(
                "VALIDATION_ERROR",
                "Validation failed: %s".formatted(fieldOrConcept),
                reason);
    }

    /**
     * Not found error: the requested resource does not exist.
     * The identifier goes in {@code details} so localized messages can still show it.
     */
    public static ApplicationError notFound(String resourceType, String identifier) {
        return new ApplicationError(
                "%s_NOT_FOUND".formatted(toCodeSegment(resourceType)),
                "%s not found: %s".formatted(resourceType, identifier),
                identifier);
    }

    /**
     * Business rule violation error: operation violates domain constraints
     */
    public static ApplicationError businessRuleViolation(String rule, String reason) {
        return new ApplicationError(
                "BUSINESS_RULE_VIOLATION",
                "Business rule violation: %s".formatted(rule),
                reason);
    }

    /**
     * Conflict error: operation cannot be completed due to conflicting state
     */
    public static ApplicationError conflict(String resource, String reason) {
        return new ApplicationError(
                "%s_CONFLICT".formatted(toCodeSegment(resource)),
                "Conflict with %s".formatted(resource),
                reason);
    }

    /**
     * Forbidden error: the authenticated user cannot act on this resource
     * (e.g., a store admin accessing data of another store).
     */
    public static ApplicationError forbidden(String resource, String reason) {
        return new ApplicationError(
                "FORBIDDEN",
                "Access denied to %s".formatted(resource),
                reason);
    }

    /**
     * Unauthorized error: the request is not authenticated
     */
    public static ApplicationError unauthorized(String reason) {
        return new ApplicationError(
                "UNAUTHORIZED",
                "Authentication required",
                reason);
    }

    /**
     * Unexpected error: something went wrong that shouldn't have
     */
    public static ApplicationError unexpected(String context, String reason) {
        return new ApplicationError(
                "UNEXPECTED_ERROR",
                "Unexpected error in %s".formatted(context),
                reason);
    }

    /**
     * Converts a resource name to an UPPER_SNAKE_CASE code segment:
     * {@code "CreditSale"} or {@code "credit-sale"} becomes {@code "CREDIT_SALE"}.
     */
    private static String toCodeSegment(String resource) {
        return resource
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[\\s-]+", "_")
                .toUpperCase(Locale.ROOT);
    }
}
