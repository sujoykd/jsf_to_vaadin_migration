package br.com.webbudget.vaadin.views.registration;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BalanceHistoricViewTest {
    @Test
    void components_are_present() {
        var view = new BalanceHistoricView(mock(BalanceHistoricPresenter.class));
        assertThat(view.walletNameField).isNotNull();
        assertThat(view.historicGrid).isNotNull();
        assertThat(view.backButton).isNotNull();
    }
    @Test
    void grid_has_columns() {
        var view = new BalanceHistoricView(mock(BalanceHistoricPresenter.class));
        assertThat(view.historicGrid.getColumns()).hasSize(6);
    }
    @Test
    void wallet_name_field_is_read_only() {
        var view = new BalanceHistoricView(mock(BalanceHistoricPresenter.class));
        assertThat(view.walletNameField.isReadOnly()).isTrue();
    }
}
