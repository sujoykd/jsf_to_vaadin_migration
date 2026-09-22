package br.com.webbudget.vaadin.views;

import br.com.webbudget.infrastructure.i18n.MessageSource;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessDeniedViewTest {

    @BeforeAll
    @SuppressWarnings("JavaReflectionMemberAccess")
    static void initMessageSource() throws Exception {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setDefaultLocale(Locale.of("en", "US"));

        Field delegateField = MessageSource.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        delegateField.set(null, ms);
    }

    @Test
    void view_has_401_status_code_heading() {
        var view = new AccessDeniedView();
        assertThat(view.statusCode.getText()).isEqualTo("401");
    }

    @Test
    @SuppressWarnings("unchecked")
    void setErrorParameter_returns_401_status_code() {
        var view = new AccessDeniedView();
        var event = mock(BeforeEnterEvent.class);
        var param = (ErrorParameter<AccessDeniedException>) mock(ErrorParameter.class);
        when(param.getException()).thenReturn(new AccessDeniedException("forbidden"));

        int status = view.setErrorParameter(event, param);

        assertThat(status).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void setErrorParameter_populates_message_and_details_from_i18n() {
        var view = new AccessDeniedView();
        var event = mock(BeforeEnterEvent.class);
        var param = (ErrorParameter<AccessDeniedException>) mock(ErrorParameter.class);
        when(param.getException()).thenReturn(new AccessDeniedException("forbidden"));

        view.setErrorParameter(event, param);

        assertThat(view.message.getText()).isNotBlank().doesNotStartWith("$$");
        assertThat(view.details.getText()).isNotBlank().doesNotStartWith("$$");
    }

    @Test
    void i18n_messages_exist_in_en_US() {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");

        var msg = ms.getMessage("401.message", null, Locale.of("en", "US"));
        assertThat(msg).isEqualTo("Not authorized");

        var details = ms.getMessage("401.details", null, Locale.of("en", "US"));
        assertThat(details).contains("permission");
    }

    @Test
    void i18n_messages_exist_in_pt_BR() {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");

        var msg = ms.getMessage("401.message", null, Locale.of("pt", "BR"));
        assertThat(msg).isNotBlank();

        var details = ms.getMessage("401.details", null, Locale.of("pt", "BR"));
        assertThat(details).isNotBlank();
    }
}
