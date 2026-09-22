package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.financial.InvoiceState;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "financial/credit-card-invoices", layout = MainLayout.class)
@PageTitle("Card Invoices")
public class ListCreditCardInvoicesView extends VerticalLayout {

    final Grid<CreditCardInvoice> grid = new Grid<>(CreditCardInvoice.class, false);
    final TextField filterField = new TextField();
    final ComboBox<InvoiceState> stateFilter = new ComboBox<>();

    public ListCreditCardInvoicesView(ListCreditCardInvoicesPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(CreditCardInvoice::getIdentification).setHeader("Identification").setSortable(true);
        grid.addColumn(invoice -> invoice.getCard() != null ? invoice.getCard().getName() : "")
                .setHeader("Card").setSortable(true);
        grid.addColumn(CreditCardInvoice::getTotalValue).setHeader("Amount").setSortable(true);
        grid.addColumn(CreditCardInvoice::getDueDate).setHeader("Due Date").setSortable(true);
        grid.addColumn(invoice -> invoice.getInvoiceState() != null ? invoice.getInvoiceState().toString() : "")
                .setHeader("Status").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(invoice -> {
            var closeBtn = new Button("Close");
            closeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            closeBtn.setEnabled(invoice.isOpen());
            closeBtn.addClickListener(e -> {
                presenter.close(invoice.getId());
                grid.getDataProvider().refreshAll();
                var notification = Notification.show("Invoice closed successfully.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(3000);
            });

            var detailsBtn = new Button("Details");
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            detailsBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailCreditCardInvoiceView.class,
                            new RouteParameters("id", String.valueOf(invoice.getId()))));

            var payBtn = new Button("Pay");
            payBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            payBtn.setEnabled(invoice.isClosed());
            payBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormPaymentView.class,
                            new RouteParameters("id", String.valueOf(invoice.getId()))));

            return new HorizontalLayout(closeBtn, payBtn, detailsBtn);
        })).setHeader("Actions");

        grid.setDataProvider(new CallbackDataProvider<>(
                query -> presenter.findAll(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        stateFilter.getValue(),
                        query.getOffset(),
                        query.getPageSize()).getContent().stream(),
                query -> presenter.count(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        stateFilter.getValue())
        ));
        grid.setSizeFull();

        filterField.setPlaceholder("Filter...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        stateFilter.setPlaceholder("State...");
        stateFilter.setItems(InvoiceState.values());
        stateFilter.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        var clearBtn = new Button("Clear Filters", e -> {
            filterField.clear();
            stateFilter.clear();
            grid.getDataProvider().refreshAll();
        });
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(filterField, stateFilter, clearBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
