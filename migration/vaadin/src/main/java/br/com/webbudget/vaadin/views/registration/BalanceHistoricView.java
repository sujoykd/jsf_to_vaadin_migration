package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.financial.WalletBalance;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/wallets/balance-historic/:id", layout = MainLayout.class)
@PageTitle("Balance Historic")
public class BalanceHistoricView extends VerticalLayout implements BeforeEnterObserver {

    final TextField walletNameField = new TextField("Wallet");
    final Grid<WalletBalance> historicGrid = new Grid<>(WalletBalance.class, false);
    final Button backButton = new Button("Back");

    private final BalanceHistoricPresenter presenter;

    public BalanceHistoricView(BalanceHistoricPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("1100px");
        walletNameField.setReadOnly(true); walletNameField.setWidth("300px");

        historicGrid.addColumn(wb -> wb.getMovementDateTime() != null ? wb.getMovementDateTime().toString() : "")
                .setHeader("Date/Time").setSortable(true);
        historicGrid.addColumn(wb -> wb.getReasonType() != null ? wb.getReasonType().name() : "").setHeader("Reason");
        historicGrid.addColumn(wb -> wb.getBalanceType() != null ? wb.getBalanceType().name() : "").setHeader("Balance Type");
        historicGrid.addColumn(wb -> wb.getTransactionValue() != null ? wb.getTransactionValue().toPlainString() : "")
                .setHeader("Transaction Value");
        historicGrid.addColumn(wb -> wb.getActualBalance() != null ? wb.getActualBalance().toPlainString() : "")
                .setHeader("Actual Balance");
        historicGrid.addColumn(wb -> wb.getObservations() != null ? wb.getObservations() : "").setHeader("Observations");
        historicGrid.setHeight("400px");

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/wallets"));

        add(new HorizontalLayout(walletNameField), historicGrid, new HorizontalLayout(backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadWallet, () -> event.forwardTo("registration/wallets"));
    }

    private void loadWallet(Wallet wallet) {
        walletNameField.setValue(wallet.getName() != null ? wallet.getName() : "");
        historicGrid.setItems(presenter.findHistoricByWalletId(wallet.getId()));
    }
}
