package pe.edu.upc.center.creditotiendas.iam.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.edu.upc.center.creditotiendas.iam.application.internal.outboundservices.tokens.TokenService;

import java.util.List;

/**
 * Security rules of the API: stateless JWT authentication, role checks with
 * {@code @PreAuthorize} on controllers, and CORS for the frontend.
 *
 * <ul>
 *   <li>CSRF is disabled because there are no cookies or sessions: the token travels in the
 *       {@code Authorization} header, which a third-party site cannot attach.</li>
 *   <li>Only sign-in, Swagger, health check and the error page are public. Everything else
 *       requires a valid token.</li>
 *   <li>The H2 console is only opened when {@code spring.h2.console.enabled=true} (dev).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/authentication/sign-in",
            "/swagger-ui.html", "/swagger-ui/**",
            "/api-docs", "/api-docs/**",
            "/actuator/health", "/actuator/health/**",
            "/error"
    };

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final RestSecurityExceptionHandler restSecurityExceptionHandler;
    private final List<String> allowedOrigins;
    private final boolean h2ConsoleEnabled;

    public WebSecurityConfiguration(TokenService tokenService,
                                    UserDetailsService userDetailsService,
                                    RestSecurityExceptionHandler restSecurityExceptionHandler,
                                    @Value("${cors.allowed-origins}") String[] allowedOrigins,
                                    @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.restSecurityExceptionHandler = restSecurityExceptionHandler;
        this.allowedOrigins = List.of(allowedOrigins);
        this.h2ConsoleEnabled = h2ConsoleEnabled;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
                    if (h2ConsoleEnabled) authorize.requestMatchers("/h2-console/**").permitAll();
                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(new BearerAuthorizationRequestFilter(tokenService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        // The H2 console renders inside frames; everything else keeps the default DENY
        if (h2ConsoleEnabled) http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Accept-Language"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
