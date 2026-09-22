package br.com.webbudget.vaadin.views;

import br.com.webbudget.infrastructure.i18n.MessageSource;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

@AnonymousAllowed
@PageTitle("404 - Page Not Found")
public class RouteNotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    H1 statusCode;
    Span message;
    Paragraph details;

    public RouteNotFoundView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        statusCode = new H1("404");
        message = new Span();
        details = new Paragraph();

        add(statusCode, message, details);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        message.setText(MessageSource.get("404.message"));
        details.setText(MessageSource.get("404.details"));
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
