package br.com.webbudget.vaadin.layout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class LoginLayout extends Div implements RouterLayout {

    public LoginLayout() {
        setSizeFull();
    }
}
