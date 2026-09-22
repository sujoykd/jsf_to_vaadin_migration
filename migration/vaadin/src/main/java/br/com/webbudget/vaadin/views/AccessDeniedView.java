package br.com.webbudget.vaadin.views;

import br.com.webbudget.infrastructure.i18n.MessageSource;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

@AnonymousAllowed
@PageTitle("401 - Not Authorized")
public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    H1 statusCode;
    Span message;
    Paragraph details;

    public AccessDeniedView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        statusCode = new H1("401");
        message = new Span();
        details = new Paragraph();

        add(statusCode, message, details);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        message.setText(MessageSource.get("401.message"));
        details.setText(MessageSource.get("401.details"));
        return HttpServletResponse.SC_UNAUTHORIZED;
    }
}
