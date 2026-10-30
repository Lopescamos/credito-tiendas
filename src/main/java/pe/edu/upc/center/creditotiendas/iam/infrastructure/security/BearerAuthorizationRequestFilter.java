package pe.edu.upc.center.creditotiendas.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.tokens.TokenService;

import java.io.IOException;

/**
 * Authenticates requests that carry {@code Authorization: Bearer <jwt>}.
 *
 * <p>The user is reloaded from the database on every request, so a deactivated user or a role
 * change takes effect immediately even if the token has not expired yet. A missing or invalid
 * token never fails here: the request simply continues unauthenticated and the security rules
 * answer 401 when the endpoint is protected.</p>
 *
 * <p>Not a Spring bean on purpose: as a bean, Spring Boot would also register it as a plain
 * servlet filter and run it twice.</p>
 */
@Slf4j
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var token = header.substring(BEARER_PREFIX.length()).trim();
            tokenService.extractSubject(token).ifPresent(subject -> authenticate(subject, request));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String subject, HttpServletRequest request) {
        try {
            var userDetails = userDetailsService.loadUserByUsername(subject);
            if (!userDetails.isEnabled()) {
                log.debug("Rejected token of deactivated user {}", subject);
                return;
            }
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (UsernameNotFoundException e) {
            log.debug("Rejected token of unknown user {}", subject);
        }
    }
}
