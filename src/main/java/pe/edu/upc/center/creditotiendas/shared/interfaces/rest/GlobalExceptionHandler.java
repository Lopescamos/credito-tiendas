package pe.edu.upc.center.creditotiendas.shared.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.resources.ErrorResource;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ErrorResponseAssembler;

import java.util.stream.Stream;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * HTTP responses via the shared error assembly pattern.
 *
 * <p>Command services return {@code Result} instead of throwing, so this handler only
 * covers what happens outside them: request binding/validation, value object
 * constructors rejecting input, method security, and genuine bugs.</p>
 */
@Slf4j
@NullMarked
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions from Spring's request body validation ({@code @Valid}).
     *
     * @param ex the validation exception from @Valid binding
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var bindingResult = ex.getBindingResult();
        var errorDetails = Stream.concat(
                        bindingResult.getFieldErrors().stream()
                                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage())),
                        bindingResult.getGlobalErrors().stream()
                                .map(error -> String.valueOf(error.getDefaultMessage())))
                .reduce((a, b) -> a + "; " + b)
                .orElse("Request validation failed");

        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles a request body that cannot be parsed (malformed JSON, wrong types, invalid enum value).
     *
     * @param ex the parsing exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResource> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        var applicationError = ApplicationError.validationError(
                "request-body", "Malformed or unreadable request body");
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles a path variable or request parameter of the wrong type (e.g. {@code /products/abc}).
     *
     * @param ex the type mismatch exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var applicationError = ApplicationError.validationError(
                ex.getName(), "Invalid value '%s'".formatted(ex.getValue()));
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid arguments, typically thrown by value object constructors
     * (e.g. {@code new Ruc("123")}) while building a command from a request.
     *
     * @param ex the illegal argument exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResource> handleIllegalArgumentException(IllegalArgumentException ex) {
        var applicationError = ApplicationError.validationError(
                "request-argument",
                ex.getMessage() != null ? ex.getMessage() : "Request validation failed");
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles method security denials ({@code @PreAuthorize}), which otherwise would end up as 500.
     *
     * @param ex the access denied exception
     * @return error response with FORBIDDEN status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResource> handleAccessDenied(AccessDeniedException ex) {
        var applicationError = ApplicationError.forbidden("resource", "Insufficient permissions");
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles authentication failures raised inside a controller call.
     *
     * @param ex the authentication exception
     * @return error response with UNAUTHORIZED status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResource> handleAuthentication(AuthenticationException ex) {
        var applicationError = ApplicationError.unauthorized(
                ex.getMessage() != null ? ex.getMessage() : "Authentication failed");
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Final fallback. Spring MVC exceptions that already carry an HTTP status
     * (404 unknown route, 405 wrong method, 415 unsupported media type, missing parameter...)
     * keep that status. Anything else is a bug: it is logged and answered with a generic 500
     * that never exposes internal exception messages to the client.
     *
     * @param ex the exception
     * @return error response with the exception's own status, or INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleException(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return toErrorResponseFromSpringErrorResponse(errorResponse);
        }
        log.error("Unhandled exception while processing request", ex);
        var applicationError = ApplicationError.unexpected("request-processing", "An unexpected error occurred");
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    private ResponseEntity<ErrorResource> toErrorResponseFromSpringErrorResponse(ErrorResponse errorResponse) {
        var status = errorResponse.getStatusCode();
        var detail = errorResponse.getBody().getDetail();
        if (status.value() == HttpStatus.BAD_REQUEST.value()) {
            var applicationError = ApplicationError.validationError(
                    "request", detail != null ? detail : "Request validation failed");
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
        }
        var code = status instanceof HttpStatus httpStatus ? httpStatus.name() : "HTTP_" + status.value();
        var message = detail != null ? detail : "Request could not be processed";
        return ResponseEntity.status(status).body(new ErrorResource(code, message));
    }
}
