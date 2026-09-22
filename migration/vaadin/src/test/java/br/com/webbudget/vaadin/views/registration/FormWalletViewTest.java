package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FormWalletViewTest {
    @Test
    void fields_are_present() {
        var view = new FormWalletView(mock(FormWalletPresenter.class));
        assertThat(view.nameField).isNotNull();
        assertThat(view.walletTypeSelect).isNotNull();
        assertThat(view.actualBalanceField).isNotNull();
    }
    @Test
    void required_fields_are_marked() {
        var view = new FormWalletView(mock(FormWalletPresenter.class));
        assertThat(view.nameField.isRequiredIndicatorVisible()).isTrue();
        assertThat(view.actualBalanceField.isRequiredIndicatorVisible()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new FormWalletView(mock(FormWalletPresenter.class));
        assertThat(view.saveButton).isNotNull();
        assertThat(view.updateButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
