package br.com.webbudget.vaadin.views.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetailUserViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(DetailUserPresenter.class);
        when(presenter.findById(0L)).thenReturn(Optional.empty());
        when(presenter.findAllGroups()).thenReturn(List.of());

        var view = new DetailUserView(presenter);

        assertThat(view.nameField).isNotNull();
        assertThat(view.usernameField).isNotNull();
        assertThat(view.emailField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
        assertThat(view.groupField).isNotNull();
    }

    @Test
    void all_fields_are_read_only() {
        var presenter = mock(DetailUserPresenter.class);
        when(presenter.findById(0L)).thenReturn(Optional.empty());
        when(presenter.findAllGroups()).thenReturn(List.of());

        var view = new DetailUserView(presenter);

        assertThat(view.nameField.isReadOnly()).isTrue();
    }
}
