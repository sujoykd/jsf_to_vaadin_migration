package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.Wallet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListWalletsViewTest {

    @SuppressWarnings("unchecked")
    @Test
    void grid_has_4_columns() {
        var presenter = mock(ListWalletsPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListWalletsView(presenter);

        assertThat(view.grid.getColumns()).hasSize(4);
    }

    @SuppressWarnings("unchecked")
    @Test
    void filter_field_is_present() {
        var presenter = mock(ListWalletsPresenter.class);
        when(presenter.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.of(List.of(), 0));
        when(presenter.count(any(), any())).thenReturn(0);

        var view = new ListWalletsView(presenter);

        assertThat(view.filterField).isNotNull();
        assertThat(view.filterField.isClearButtonVisible()).isTrue();
    }
}
