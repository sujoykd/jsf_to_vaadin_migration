package br.com.webbudget.infrastructure.spring.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames(
            "classpath:i18n/messages",
            "classpath:i18n/menu",
            "classpath:i18n/enums",
            "classpath:i18n/breadcrumb",
            "classpath:i18n/permissions"
        );
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setDefaultLocale(Locale.of("pt", "BR"));
        return ms;
    }

    @Bean
    public I18NProvider i18NProvider(MessageSource messageSource) {
        return new I18NProvider() {

            private static final List<Locale> LOCALES = List.of(
                Locale.of("pt", "BR"),
                Locale.of("en", "US")
            );

            @Override
            public List<Locale> getProvidedLocales() {
                return LOCALES;
            }

            @Override
            public String getTranslation(String key, Locale locale, Object... params) {
                try {
                    String msg = messageSource.getMessage(key, null, locale);
                    return params.length > 0 ? MessageFormat.format(msg, params) : msg;
                } catch (MissingResourceException e) {
                    return key;
                }
            }
        };
    }
}
