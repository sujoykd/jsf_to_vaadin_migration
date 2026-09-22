package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Group;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormUserViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(FormUserPresenter.class);
        when(presenter.findAllGroups()).thenReturn(List.of());
        when(presenter.findById(0L)).thenReturn(Optional.empty());

        var view = new FormUserView(presenter);

        assertThat(view.nameField).isNotNull();
        assertThat(view.usernameField).isNotNull();
        assertThat(view.emailField).isNotNull();
        assertThat(view.passwordField).isNotNull();
        assertThat(view.groupComboBox).isNotNull();
        assertThat(view.storeTypeComboBox).isNotNull();
    }

    @Test
    void group_combo_is_populated() {
        var group = mock(Group.class);
        when(group.getName()).thenReturn("Administrators");

        var presenter = mock(FormUserPresenter.class);
        when(presenter.findAllGroups()).thenReturn(List.of(group));
        when(presenter.findById(0L)).thenReturn(Optional.empty());

        var view = new FormUserView(presenter);

        assertThat(view.groupComboBox).isNotNull();
    }
}
