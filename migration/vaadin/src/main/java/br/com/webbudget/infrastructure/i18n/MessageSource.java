package br.com.webbudget.infrastructure.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Static accessor for Spring's MessageSource — keeps the same API as the JSF version so domain
 * entities that call MessageSource.get(...) continue to compile unchanged.
 */
@Component("messageSourceAccessor")
public class MessageSource {

    private static final Logger logger = LoggerFactory.getLogger(MessageSource.class);

    private static org.springframework.context.MessageSource delegate;

    public MessageSource(org.springframework.context.MessageSource messageSource) {
        MessageSource.delegate = messageSource;
    }

    public static String get(String key) {
        try {
            return delegate.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception ex) {
            logger.warn("No message found for key {}", key);
            return "$$" + key + "$$";
        }
    }

    public static String get(String key, Object... parameters) {
        try {
            return delegate.getMessage(key, parameters, LocaleContextHolder.getLocale());
        } catch (Exception ex) {
            logger.warn("No message found for key {}", key);
            return "$$" + key + "$$";
        }
    }
}
