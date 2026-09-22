package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.filter.TransferenceFilter;
import br.com.webbudget.domain.entities.financial.Transference;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route(value = "financial/transference-historic", layout = MainLayout.class)
@PageTitle("Transfer History")
public class TransferenceHistoricView extends VerticalLayout {

    final Grid<Transference> grid = new Grid<>(Transference.class, false);
    final ComboBox<Wallet> originFilter = new ComboBox<>("Origin");
    final ComboBox<Wallet> destinationFilter = new ComboBox<>("Destination");
    final DatePicker dateFilter = new DatePicker("Date");

    private List<Transference> currentData = List.of();

    public TransferenceHistoricView(TransferenceHistoricPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        List<Wallet> wallets = presenter.loadWallets();

        originFilter.setItemLabelGenerator(Wallet::getName);
        originFilter.setItems(wallets);
        originFilter.setClearButtonVisible(true);

        destinationFilter.setItemLabelGenerator(Wallet::getName);
        destinationFilter.setItems(wallets);
        destinationFilter.setClearButtonVisible(true);

        dateFilter.setClearButtonVisible(true);

        grid.addColumn(Transference::getTransferDate).setHeader("Operation Date").setSortable(true);
        grid.addColumn(Transference::getValue).setHeader("Transferred Amount").setSortable(true);
        grid.addColumn(t -> t.getOrigin() != null ? t.getOrigin().getName() : "")
                .setHeader("Origin").setSortable(true);
        grid.addColumn(t -> t.getDestination() != null ? t.getDestination().getName() : "")
                .setHeader("Destination").setSortable(true);
        grid.addColumn(Transference::getDescription).setHeader("Description").setSortable(true);
        grid.setSizeFull();
        grid.setItems(currentData);

        var filterBtn = new Button("Filter", e -> {
            var filter = buildFilter();
            currentData = presenter.filter(filter);
            grid.setItems(currentData);
        });
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var clearBtn = new Button("Clear", e -> {
            originFilter.clear();
            destinationFilter.clear();
            dateFilter.clear();
            var emptyFilter = new TransferenceFilter();
            currentData = presenter.filter(emptyFilter);
            grid.setItems(currentData);
        });
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var backBtn = new Button("Back", e -> UI.getCurrent().navigate(FormTransferenceView.class));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var filterBar = new HorizontalLayout(originFilter, destinationFilter, dateFilter, filterBtn, clearBtn, backBtn);
        filterBar.setWidthFull();
        filterBar.setAlignItems(Alignment.BASELINE);
        filterBar.setJustifyContentMode(JustifyContentMode.START);
        filterBar.setPadding(true);
        filterBar.setSpacing(true);

        add(filterBar, grid);
        expand(grid);
    }

    private TransferenceFilter buildFilter() {
        var filter = new TransferenceFilter();
        filter.setOriginWallet(originFilter.getValue());
        filter.setDestinationWallet(destinationFilter.getValue());
        filter.setOperationDate(dateFilter.getValue());
        return filter;
    }
}
