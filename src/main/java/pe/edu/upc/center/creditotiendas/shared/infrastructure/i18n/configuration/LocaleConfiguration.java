package pe.edu.upc.center.creditotiendas.shared.infrastructure.i18n.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configures locale resolution for REST requests from the {@code Accept-Language} header.
 *
 * <p>Spanish is the default because the application targets Peruvian businesses and their
 * customers; English is still served when the client explicitly asks for it.</p>
 */
@Configuration
public class LocaleConfiguration {

    public static final Locale SPANISH = Locale.forLanguageTag("es");

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(SPANISH);
        resolver.setSupportedLocales(List.of(SPANISH, Locale.ENGLISH));
        return resolver;
    }
}
