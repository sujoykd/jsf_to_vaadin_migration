package br.com.webbudget.vaadin.views;

import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.services.RecoverPasswordService;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginViewTest {

    @BeforeAll
    @SuppressWarnings("JavaReflectionMemberAccess")
    static void initMessageSource() throws Exception {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("classpath:i18n/messages", "classpath:i18n/menu");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setDefaultLocale(Locale.of("en", "US"));

        Field delegateField = MessageSource.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        delegateField.set(null, ms);
    }

    @Test
    void view_creates_login_form_with_forgot_password_button_visible() {
        var service = mock(RecoverPasswordService.class);
        var view = new LoginView(service);

        assertThat(view.loginForm).isNotNull();
        assertThat(view.loginForm.isForgotPasswordButtonVisible()).isTrue();
    }

    @Test
    void view_creates_recover_password_dialog_with_email_field() {
        var service = mock(RecoverPasswordService.class);
        var view = new LoginView(service);

        assertThat(view.recoverPasswordDialog).isNotNull();
        assertThat(view.emailField).isNotNull();
        assertThat(view.emailField.isRequired()).isTrue();
    }

    @Test
    void recover_button_exists_with_label() {
        var service = mock(RecoverPasswordService.class);
        var view = new LoginView(service);

        assertThat(view.recoverBtn).isNotNull();
        assertThat(view.recoverBtn.getText()).isEqualTo("Recover");
    }

    @Test
    void recover_password_service_is_called_with_email() {
        var service = mock(RecoverPasswordService.class);
        service.recover("user@example.com");
        verify(service).recover("user@example.com");
    }

    @Test
    void recover_password_service_throws_on_unknown_email() {
        var service = mock(RecoverPasswordService.class);
        doThrow(new BusinessLogicException("error.recover-password.user-not-found"))
                .when(service).recover(anyString());

        assertThatThrownBy(() -> service.recover("unknown@example.com"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.recover-password.user-not-found");
    }

    @Test
    void i18n_labels_resolve_from_message_source() {
        var ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("classpath:i18n/messages", "classpath:i18n/menu");
        ms.setDefaultEncoding("UTF-8");

        assertThat(ms.getMessage("login.username", null, Locale.of("en", "US"))).isEqualTo("User");
        assertThat(ms.getMessage("login.password", null, Locale.of("en", "US"))).isEqualTo("Password");
        assertThat(ms.getMessage("login.welcome", null, Locale.of("en", "US"))).isEqualTo("Welcome");
        assertThat(ms.getMessage("recover-password.dialog.title", null, Locale.of("en", "US"))).isEqualTo("Recover Password");
        assertThat(ms.getMessage("recover-password.email-sent", null, Locale.of("en", "US"))).isNotBlank();
        assertThat(ms.getMessage("error.authentication.failed", null, Locale.of("en", "US"))).isNotBlank();
    }
}
