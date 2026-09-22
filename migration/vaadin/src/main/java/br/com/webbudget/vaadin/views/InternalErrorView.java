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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnonymousAllowed
@PageTitle("500 - Internal Server Error")
public class InternalErrorView extends VerticalLayout implements HasErrorParameter<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(InternalErrorView.class);

    H1 statusCode;
    Span message;
    Paragraph details;

    public InternalErrorView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        statusCode = new H1("500");
        message = new Span();
        details = new Paragraph();

        add(statusCode, message, details);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        logger.error("Internal server error on route: {}", event.getNavigationTarget(), parameter.getCaughtException());
        message.setText(MessageSource.get("500.message"));
        details.setText(MessageSource.get("500.details"));
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
