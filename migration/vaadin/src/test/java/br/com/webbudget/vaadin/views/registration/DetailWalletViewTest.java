package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailWalletViewTest {
    @Test
    void fields_are_present() {
        var view = new DetailWalletView(mock(DetailWalletPresenter.class));
        assertThat(view.nameField).isNotNull();
        assertThat(view.walletTypeField).isNotNull();
        assertThat(view.balanceField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
    }
    @Test
    void fields_are_read_only() {
        var view = new DetailWalletView(mock(DetailWalletPresenter.class));
        assertThat(view.nameField.isReadOnly()).isTrue();
        assertThat(view.balanceField.isReadOnly()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new DetailWalletView(mock(DetailWalletPresenter.class));
        assertThat(view.editButton).isNotNull();
        assertThat(view.deleteButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
