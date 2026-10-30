package pe.edu.upc.center.creditotiendas.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.interfaces.rest.transform.ErrorResponseAssembler;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Answers 401 (no valid token) and 403 (valid token, insufficient role) raised by the security
 * filter chain with the same {@code ErrorResource} body as the rest of the API, instead of
 * Spring Security's default empty responses.
 */
@Component
public class RestSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;
    private final LocaleResolver localeResolver;

    public RestSecurityExceptionHandler(JsonMapper jsonMapper, LocaleResolver localeResolver) {
        this.jsonMapper = jsonMapper;
        this.localeResolver = localeResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(request, response, ApplicationError.unauthorized("A valid bearer token is required"));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(request, response, ApplicationError.forbidden("resource", "Insufficient permissions"));
    }

    private void write(HttpServletRequest request, HttpServletResponse response, ApplicationError error)
            throws IOException {
        // The filter chain runs before DispatcherServlet, so apply the app's locale rules here
        var previousLocale = LocaleContextHolder.getLocaleContext();
        LocaleContextHolder.setLocale(localeResolver.resolveLocale(request));
        try {
            var errorResponse = ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
            response.setStatus(errorResponse.getStatusCode().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(jsonMapper.writeValueAsString(errorResponse.getBody()));
        } finally {
            LocaleContextHolder.setLocaleContext(previousLocale);
        }
    }
}
