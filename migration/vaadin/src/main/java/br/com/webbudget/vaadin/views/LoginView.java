package br.com.webbudget.vaadin.views;

import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.services.RecoverPasswordService;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import br.com.webbudget.vaadin.layout.LoginLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route(value = "login", layout = LoginLayout.class)
@PageTitle("webBudget")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    LoginForm loginForm;
    Dialog recoverPasswordDialog;
    TextField emailField;
    Button recoverBtn;

    public LoginView(RecoverPasswordService recoverPasswordService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        emailField = new TextField(MessageSource.get("recover-password.form.email"));
        emailField.setRequired(true);

        recoverBtn = new Button(MessageSource.get("recover"), clickEvent -> {
            try {
                recoverPasswordService.recover(emailField.getValue());
                recoverPasswordDialog.close();
                Notification.show(MessageSource.get("recover-password.email-sent"));
            } catch (BusinessLogicException ex) {
                Notification.show(MessageSource.get(ex.getMessage()));
            }
        });

        recoverPasswordDialog = new Dialog();
        recoverPasswordDialog.setHeaderTitle(MessageSource.get("recover-password.dialog.title"));
        recoverPasswordDialog.add(emailField);
        recoverPasswordDialog.getFooter().add(recoverBtn);

        loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(true);
        loginForm.setI18n(buildI18n());
        loginForm.addForgotPasswordListener(e -> recoverPasswordDialog.open());

        add(new H2("web::budget"), loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }

    private LoginI18n buildI18n() {
        var i18n = LoginI18n.createDefault();

        var header = new LoginI18n.Header();
        header.setTitle("web::budget");
        header.setDescription(MessageSource.get("login.welcome"));
        i18n.setHeader(header);

        var form = i18n.getForm();
        form.setUsername(MessageSource.get("login.username"));
        form.setPassword(MessageSource.get("login.password"));
        form.setForgotPassword(MessageSource.get("login.recover-password"));
        form.setSubmit(MessageSource.get("login"));

        var errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle("Error");
        errorMessage.setMessage(MessageSource.get("error.authentication.failed"));
        i18n.setErrorMessage(errorMessage);

        return i18n;
    }
}
