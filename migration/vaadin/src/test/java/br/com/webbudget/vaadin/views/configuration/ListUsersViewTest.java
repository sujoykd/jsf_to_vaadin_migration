package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.configuration.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class ListUsersViewTest {

    @Test
    void grid_has_expected_columns() {
        var presenter = mock(ListUsersPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListUsersView(presenter);

        assertThat(view.grid.getColumns()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void filter_field_is_present() {
        var presenter = mock(ListUsersPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListUsersView(presenter);

        assertThat(view.filterField).isNotNull();
        assertThat(view.filterField.isClearButtonVisible()).isTrue();
    }
}
