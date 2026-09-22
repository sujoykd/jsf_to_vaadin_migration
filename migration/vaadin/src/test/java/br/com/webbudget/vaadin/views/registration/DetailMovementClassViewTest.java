package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DetailMovementClassViewTest {
    @Test
    void fields_are_present() {
        var view = new DetailMovementClassView(mock(DetailMovementClassPresenter.class));
        assertThat(view.nameField).isNotNull();
        assertThat(view.typeField).isNotNull();
        assertThat(view.costCenterField).isNotNull();
        assertThat(view.budgetField).isNotNull();
    }
    @Test
    void fields_are_read_only() {
        var view = new DetailMovementClassView(mock(DetailMovementClassPresenter.class));
        assertThat(view.nameField.isReadOnly()).isTrue();
        assertThat(view.typeField.isReadOnly()).isTrue();
    }
    @Test
    void action_buttons_are_present() {
        var view = new DetailMovementClassView(mock(DetailMovementClassPresenter.class));
        assertThat(view.editButton).isNotNull();
        assertThat(view.deleteButton).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
}
