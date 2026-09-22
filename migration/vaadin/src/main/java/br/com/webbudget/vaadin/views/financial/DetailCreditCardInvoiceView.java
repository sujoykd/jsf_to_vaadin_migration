package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "financial/credit-card-invoices/:id", layout = MainLayout.class)
@PageTitle("Card Invoices")
public class DetailCreditCardInvoiceView extends VerticalLayout implements BeforeEnterObserver {

    final TextField identificationField = new TextField("Identification");
    final TextField cardField = new TextField("Card");
    final TextField financialPeriodField = new TextField("Financial Period");
    final TextField totalValueField = new TextField("Total Value");
    final TextField stateField = new TextField("State");
    final Grid<PeriodMovement> movementsGrid = new Grid<>(PeriodMovement.class, false);

    private CreditCardInvoice current;
    private final DetailCreditCardInvoicePresenter presenter;

    public DetailCreditCardInvoiceView(DetailCreditCardInvoicePresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("1000px");

        identificationField.setReadOnly(true);
        identificationField.setWidthFull();

        cardField.setReadOnly(true);
        cardField.setWidthFull();

        financialPeriodField.setReadOnly(true);
        financialPeriodField.setWidthFull();

        totalValueField.setReadOnly(true);
        totalValueField.setWidthFull();

        stateField.setReadOnly(true);
        stateField.setWidthFull();

        movementsGrid.addColumn(PeriodMovement::getIdentification).setHeader("Identification");
        movementsGrid.addColumn(PeriodMovement::getDueDate).setHeader("Due Date");
        movementsGrid.addColumn(PeriodMovement::getValue).setHeader("Amount");

        var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        form.add(identificationField, cardField, financialPeriodField, totalValueField, stateField);

        var printButton = new Button("Print", e -> {
            if (current != null) {
                UI.getCurrent().navigate(CardInvoicePrintView.class,
                        new RouteParameters("id", String.valueOf(current.getId())));
            }
        });
        printButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var backButton = new Button("Back", e -> UI.getCurrent().navigate("financial/credit-card-invoices"));

        var toolbar = new HorizontalLayout(printButton, backButton);
        toolbar.setSpacing(true);

        add(toolbar, form, movementsGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadInvoice, () -> event.forwardTo("financial/credit-card-invoices"));
    }

    void loadInvoice(CreditCardInvoice invoice) {
        this.current = invoice;
        identificationField.setValue(invoice.getIdentification() != null ? invoice.getIdentification() : "");
        cardField.setValue(invoice.getCard() != null ? invoice.getCard().getName() : "");
        financialPeriodField.setValue(invoice.getFinancialPeriod() != null ? invoice.getFinancialPeriod().getIdentification() : "");
        totalValueField.setValue(invoice.getTotalValue() != null ? invoice.getTotalValue().toString() : "");
        stateField.setValue(invoice.getInvoiceState() != null ? invoice.getInvoiceState().toString() : "");
        movementsGrid.setItems(invoice.getPeriodMovements());
    }
}
