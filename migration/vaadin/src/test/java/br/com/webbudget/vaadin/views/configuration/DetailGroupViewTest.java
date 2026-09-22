package br.com.webbudget.vaadin.views.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetailGroupViewTest {

    @Test
    void fields_are_present() {
        var presenter = mock(DetailGroupPresenter.class);
        when(presenter.findById(0L)).thenReturn(Optional.empty());
        when(presenter.findAllActive()).thenReturn(List.of());

        var view = new DetailGroupView(presenter);

        assertThat(view.nameField).isNotNull();
        assertThat(view.activeCheckbox).isNotNull();
        assertThat(view.parentField).isNotNull();
    }

    @Test
    void grants_grid_is_present() {
        var presenter = mock(DetailGroupPresenter.class);
        when(presenter.findById(0L)).thenReturn(Optional.empty());
        when(presenter.findAllActive()).thenReturn(List.of());

        var view = new DetailGroupView(presenter);

        assertThat(view.grantsGrid).isNotNull();
    }

    @Test
    void grants_grid_has_two_columns() {
        var presenter = mock(DetailGroupPresenter.class);
        when(presenter.findById(0L)).thenReturn(Optional.empty());
        when(presenter.findAllActive()).thenReturn(List.of());

        var view = new DetailGroupView(presenter);

        assertThat(view.grantsGrid.getColumns()).hasSizeGreaterThanOrEqualTo(2);
    }
}
