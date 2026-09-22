package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FormContactViewTest {

    @Test
    void fields_are_present() {
        var view = new FormContactView(mock(FormContactPresenter.class));
        assertThat(view.nameField).isNotNull();
        assertThat(view.contactTypeSelect).isNotNull();
        assertThat(view.emailField).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = new FormContactView(mock(FormContactPresenter.class));
        assertThat(view.nameField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = new FormContactView(mock(FormContactPresenter.class));
        assertThat(view.saveButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
