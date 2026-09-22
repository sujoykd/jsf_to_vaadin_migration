package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailCardViewTest {

    @Test
    void fields_are_present() {
        var view = new DetailCardView(mock(DetailCardPresenter.class));
        assertThat(view.nameField).isNotNull();
        assertThat(view.numberField).isNotNull();
        assertThat(view.holderField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
    }

    @Test
    void fields_are_read_only() {
        var view = new DetailCardView(mock(DetailCardPresenter.class));
        assertThat(view.nameField.isReadOnly()).isTrue();
        assertThat(view.numberField.isReadOnly()).isTrue();
    }

    @Test
    void action_buttons_are_present() {
        var view = new DetailCardView(mock(DetailCardPresenter.class));
        assertThat(view.editButton).isNotNull();
        assertThat(view.deleteButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
