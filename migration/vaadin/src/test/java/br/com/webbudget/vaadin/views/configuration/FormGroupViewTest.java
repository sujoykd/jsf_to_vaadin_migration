package br.com.webbudget.vaadin.views.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormGroupViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(FormGroupPresenter.class);
        when(presenter.findAllGroups()).thenReturn(List.of());
        when(presenter.loadAllAuthorizations()).thenReturn(List.of());

        var view = new FormGroupView(presenter);

        assertThat(view.nameField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
        assertThat(view.parentComboBox).isNotNull();
        assertThat(view.permissionsGrid).isNotNull();
    }

    @Test
    void permissions_grid_has_correct_columns() {
        var presenter = mock(FormGroupPresenter.class);
        when(presenter.findAllGroups()).thenReturn(List.of());
        when(presenter.loadAllAuthorizations()).thenReturn(List.of());

        var view = new FormGroupView(presenter);

        assertThat(view.permissionsGrid.getColumns()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void name_field_is_required() {
        var presenter = mock(FormGroupPresenter.class);
        when(presenter.findAllGroups()).thenReturn(List.of());
        when(presenter.loadAllAuthorizations()).thenReturn(List.of());

        var view = new FormGroupView(presenter);

        assertThat(view.nameField).isNotNull();
        assertThat(view.nameField.isRequired()).isTrue();
    }
}
