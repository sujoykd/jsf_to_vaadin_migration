package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormCardViewTest {

    private FormCardView createView() {
        var presenter = mock(FormCardPresenter.class);
        when(presenter.findActiveWallets()).thenReturn(List.of());
        return new FormCardView(presenter);
    }

    @Test
    void fields_are_present() {
        var view = createView();
        assertThat(view.nameField).isNotNull();
        assertThat(view.typeSelect).isNotNull();
        assertThat(view.brandField).isNotNull();
        assertThat(view.numberField).isNotNull();
        assertThat(view.holderField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
    }

    @Test
    void required_fields_are_marked() {
        var view = createView();
        assertThat(view.nameField.isRequiredIndicatorVisible()).isTrue();
        assertThat(view.brandField.isRequiredIndicatorVisible()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = createView();
        assertThat(view.saveButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
